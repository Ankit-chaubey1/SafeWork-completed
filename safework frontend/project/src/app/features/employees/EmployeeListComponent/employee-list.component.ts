import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, NgClass, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService, EmployeeResponseDTO, EmployeeRequest } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [NgFor, NgIf, NgClass, SlicePipe, FormsModule],
  templateUrl: './employee-list.component.html',
  styles: [`.avatar { width:34px;height:34px;border-radius:50%;background:var(--primary-lighter);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;flex-shrink:0; } .fw-600{font-weight:600;}`],
})
export class EmployeeListComponent implements OnInit {
  private employeeService = inject(EmployeeService);
  private auth = inject(AuthService);
  error = signal<string | null>(null);

  search = signal('');
  filterStatus = signal('');
  showForm = signal(false);
  editingId: number | null = null;
  form: Partial<EmployeeRequest> = {};

  employees = signal<EmployeeResponseDTO[]>([]);

  canEdit = computed(() => this.auth.hasRole('Manager', 'Administrator'));

  filtered = computed(() => {
    let list = this.employees();
    const q = this.search().toLowerCase();
    const status = this.filterStatus();
    if (q) {
      list = list.filter(e => 
        e.employeeName?.toLowerCase().includes(q) || 
        e.employeeDepartmentName?.toLowerCase().includes(q) ||
        e.email?.toLowerCase().includes(q)
      );
    }
    if (status) list = list.filter(e => e.employeeStatus?.toLowerCase() === status.toLowerCase());
    return list;
  });

  ngOnInit() {
    this.loadEmployees();
  }

  async loadEmployees() {
    try {
      const data = await this.employeeService.getAllEmployees();
      this.employees.set(data || []);
      this.error.set(null);
    } catch (err: any) {
      this.error.set(err?.error?.message || 'Failed to load employees.');
    }
  }

  openForm(e?: EmployeeResponseDTO): void {
    this.editingId = e?.employeeId ?? null;
    this.form = e ? {
      userName: e.employeeName,
      userEmail: e.email,
      employeeDepartmentName: e.employeeDepartmentName,
      userStatus: e.employeeStatus,
      userRole: 'EMPLOYEE',
      employeeGender: 'Male'
    } : { userStatus: 'Pending', userRole: 'EMPLOYEE', employeeGender: 'Male' };
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingId = null;
  }

  async save() {
    if (!this.form.userName || !this.form.employeeDepartmentName || !this.form.userEmail) {
      this.error.set('Name, Email and Department are required.');
      return;
    }
    
    try {
      const dto = this.form as EmployeeRequest;
      if (this.editingId) {
        await this.employeeService.updateEmployee(this.editingId, dto);
      } else {
        if (!this.form.password) {
          this.error.set('Password is required for new employees.');
          return;
        }
        await this.employeeService.registerEmployee(dto);
      }
      this.closeForm();
      this.loadEmployees();
    } catch (err: any) {
      this.error.set(err?.error?.message || 'Failed to save employee.');
    }
  }

  async approveEmployee(employeeId: number) {
    try {
      await this.employeeService.approveEmployee(employeeId);
      this.loadEmployees();
    } catch (err: any) {
      this.error.set(err?.error?.message || 'Failed to approve employee.');
    }
  }

  initials(name: string): string { return (name || '').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2); }
  
  statusBadge(s: string): string {
    const stat = (s || '').toLowerCase();
    if (stat === 'active') return 'badge badge-success';
    if (stat === 'inactive') return 'badge badge-danger';
    if (stat === 'pending') return 'badge badge-warning';
    return 'badge badge-neutral';
  }
}
