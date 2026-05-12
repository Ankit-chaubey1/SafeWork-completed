import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InspectionService, InspectionResponseDTO, InspectionRequestDTO } from '../../core/services/inspection.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-inspection-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './inspection-list.component.html',
  styleUrls: ['./inspection-list.component.css']
})
export class InspectionListComponent implements OnInit {
  private inspectionService = inject(InspectionService);
  private auth = inject(AuthService);

  search = signal('');
  filterStatus = signal('');
  showForm = signal(false);
  selected = signal<InspectionResponseDTO | null>(null);
  form: Partial<InspectionRequestDTO> = {};
  editingInspectionId: number | null = null;

  inspections = signal<InspectionResponseDTO[]>([]);

  canAdd = computed(() => this.auth.hasRole('Safety Officer', 'Hazard Officer', 'Administrator'));

  ngOnInit() {
    this.loadInspections();
  }

  async loadInspections() {
    try {
      const data = await this.inspectionService.getAllInspections();
      this.inspections.set(data || []);
    } catch (error) {
      console.error('Failed to load inspections', error);
    }
  }

  filtered = computed(() => {
    let list = this.inspections();
    const q = this.search().toLowerCase();
    const status = this.filterStatus();
    if (q) {
      list = list.filter(i => 
        i.inspectionLocation?.toLowerCase().includes(q) || 
        i.inspectionFindings?.toLowerCase().includes(q) ||
        i.inspectionId.toString().includes(q)
      );
    }
    if (status) list = list.filter(i => i.inspectionStatus === status);
    return list;
  });

  openForm(): void {
    this.form = { inspectionStatus: 'SCHEDULED', inspectionDate: new Date().toISOString().split('T')[0] };
    this.editingInspectionId = null;
    this.showForm.set(true);
  }

  editInspection(ins: InspectionResponseDTO): void {
    this.form = { ...ins };
    if (this.form.inspectionDate) {
       this.form.inspectionDate = new Date(this.form.inspectionDate).toISOString().split('T')[0];
    }
    this.editingInspectionId = ins.inspectionId;
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); }

  async save() {
    if (!this.form.inspectionLocation || !this.form.inspectionDate) return;
    try {
      const dto = this.form as InspectionRequestDTO;
      dto.officerId = Number(this.auth.currentUser()?.userId ?? 1);
      
      if (this.editingInspectionId) {
        await this.inspectionService.updateInspection(this.editingInspectionId, dto);
      } else {
        await this.inspectionService.createInspection(dto);
      }
      this.closeForm();
      this.loadInspections();
    } catch (error) {
      console.error('Failed to save inspection', error);
    }
  }

  async deleteInspection(id: number) {
    if (confirm('Are you sure you want to delete this inspection?')) {
      try {
        await this.inspectionService.deleteInspection(id);
        this.loadInspections();
      } catch (error) {
        console.error('Failed to delete inspection', error);
      }
    }
  }

  statusBadge(s: string): string {
    const map: Record<string, string> = { SCHEDULED: 'badge badge-primary', IN_PROGRESS: 'badge badge-warning', COMPLETED: 'badge badge-success', CANCELLED: 'badge badge-danger' };
    return map[s] ?? 'badge badge-neutral';
  }

  formatStatus(s: string): string {
    if (!s) return '';
    return s.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }
}
