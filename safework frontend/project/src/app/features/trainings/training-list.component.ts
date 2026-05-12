import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TrainingService, Training } from '../../core/services/training.service';
import { ProgramService, Program } from '../../core/services/program.service';
import { EmployeeService, EmployeeResponseDTO } from '../../core/services/employee.service';
import { AuthService } from '../../core/services/auth.service';

interface TrainingView extends Training {
  employeeName: string;
  programTitle: string;
}

@Component({
  selector: 'app-training-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, FormsModule],
  templateUrl: './training-list.component.html',
  styles: [`.fw-500{font-weight:500} .text-danger{color: var(--danger)}`],
})
export class TrainingListComponent implements OnInit {
  private trainingService = inject(TrainingService);
  private programService = inject(ProgramService);
  private employeeService = inject(EmployeeService);
  private auth = inject(AuthService);

  search = signal('');
  filterStatus = signal('');
  filterProgram = signal('');
  filterEmployee = signal('');
  operationError = signal<string | null>(null);
  editingTrainingId: number | null = null;
  form: Partial<Training> = {
    programId: 0,
    employeeId: 0,
    trainingStatus: 'NOT_STARTED',
    trainingCompletionDate: '',
  };

  trainings = signal<Training[]>([]);
  programs = signal<Program[]>([]);
  employees = signal<EmployeeResponseDTO[]>([]);

  ngOnInit() {
    this.loadData();
  }

  async loadData() {
    try {
      // Fetch programs and employees gracefully, as employees might not have permission to fetch ALL employees/programs
      const [programsResult, employeesResult] = await Promise.allSettled([
        this.programService.getAllPrograms(),
        this.employeeService.getAllEmployees()
      ]);
      
      this.programs.set(programsResult.status === 'fulfilled' ? programsResult.value || [] : []);
      this.employees.set(employeesResult.status === 'fulfilled' ? employeesResult.value || [] : []);
      
      await this.loadTrainings();
    } catch (error: any) {
      this.operationError.set(error?.error?.message || 'Failed to load initial data');
    }
  }

  async loadTrainings() {
    try {
      let data: Training[];
      if (this.auth.hasRole('Employee')) {
        const uid = Number(this.auth.currentUser()?.userId ?? 0);
        data = await this.trainingService.getTrainingsByEmployee(uid);
      } else {
        data = await this.trainingService.getAllTrainings();
      }
      this.trainings.set(data || []);
      this.operationError.set(null);
    } catch (error: any) {
      this.operationError.set(error?.error?.message || 'Failed to load trainings');
    }
  }

  // Mapped list containing employee names and program titles
  mappedTrainings = computed<TrainingView[]>(() => {
    return this.trainings().map(t => {
      const emp = this.employees().find(e => Number(e.employeeId) === Number(t.employeeId));
      const prog = this.programs().find(p => Number(p.programId) === Number(t.programId));
      return {
        ...t,
        employeeName: emp ? emp.employeeName : `Emp #${t.employeeId}`,
        programTitle: prog ? prog.programTitle : `Prog #${t.programId}`
      };
    });
  });

  filtered = computed(() => {
    let list = this.mappedTrainings();
    const q = this.search().toLowerCase();
    const status = this.filterStatus();
    const progId = this.filterProgram();
    const empId = this.filterEmployee();
    
    if (q) {
      list = list.filter(t => 
        t.employeeName.toLowerCase().includes(q) || 
        t.programTitle.toLowerCase().includes(q) ||
        String(t.id).includes(q)
      );
    }
    if (status) list = list.filter(t => t.trainingStatus === status);
    if (progId) list = list.filter(t => t.programId === Number(progId));
    if (empId) list = list.filter(t => t.employeeId === Number(empId));
    return list;
  });

  completed = computed(() => this.trainings().filter(t => t.trainingStatus === 'COMPLETED').length);
  inProgress = computed(() => this.trainings().filter(t => t.trainingStatus === 'IN_PROGRESS').length);
  notStarted = computed(() => this.trainings().filter(t => t.trainingStatus === 'NOT_STARTED').length);
  failed = computed(() => this.trainings().filter(t => t.trainingStatus === 'FAILED').length);
  
  assignablePrograms = computed(() => this.programs().filter(p => p.programStatus !== 'CANCELLED'));
  assignableEmployees = computed(() => this.employees().filter(e => e.employeeStatus?.toUpperCase() !== 'TERMINATED'));

  getStatusLabel(s: string): string {
    const map: Record<string, string> = { COMPLETED: 'Completed', IN_PROGRESS: 'In Progress', NOT_STARTED: 'Not Started', FAILED: 'Failed', PLANNED: 'Planned' };
    return map[s] ?? s;
  }

  statusBadge(s: string): string {
    const map: Record<string, string> = { COMPLETED: 'badge badge-success', IN_PROGRESS: 'badge badge-info', NOT_STARTED: 'badge badge-primary', FAILED: 'badge badge-danger', PLANNED: 'badge badge-warning' };
    return map[s] ?? 'badge badge-neutral';
  }

  canAssignTraining(): boolean {
    return this.auth.hasRole('Administrator', 'Safety Officer', 'Manager');
  }

  async assignTraining() {
    if (!this.form.programId || !this.form.employeeId || Number(this.form.programId) === 0 || Number(this.form.employeeId) === 0) {
      this.operationError.set('Please select both program and employee.');
      return;
    }

    const payload: Training = {
      programId: Number(this.form.programId),
      employeeId: Number(this.form.employeeId),
      trainingCompletionDate: this.form.trainingCompletionDate || undefined,
      trainingStatus: this.form.trainingStatus || 'NOT_STARTED',
    };

    try {
      if (this.editingTrainingId) {
        payload.id = this.editingTrainingId;
        await this.trainingService.updateTraining(this.editingTrainingId, payload);
      } else {
        await this.trainingService.createTraining(payload);
      }
      
      this.form = {
        programId: 0,
        employeeId: 0,
        trainingStatus: 'NOT_STARTED',
        trainingCompletionDate: '',
      };
      this.editingTrainingId = null;
      this.loadTrainings();
    } catch (error: any) {
      this.operationError.set(error?.error?.message || 'Failed to save training.');
    }
  }

  editTraining(t: TrainingView): void {
    this.editingTrainingId = t.id!;
    this.form = {
      programId: t.programId,
      employeeId: t.employeeId,
      trainingStatus: t.trainingStatus,
      trainingCompletionDate: t.trainingCompletionDate ?? '',
    };
  }

  async deleteTraining(trainingId: number) {
    if (confirm('Are you sure you want to delete this training record?')) {
      try {
        await this.trainingService.deleteTraining(trainingId);
        if (this.editingTrainingId === trainingId) {
          this.editingTrainingId = null;
          this.form = {
            programId: 0,
            employeeId: 0,
            trainingStatus: 'NOT_STARTED',
            trainingCompletionDate: '',
          };
        }
        this.loadTrainings();
      } catch (error: any) {
        this.operationError.set(error?.error?.message || 'Failed to delete training.');
      }
    }
  }
}
