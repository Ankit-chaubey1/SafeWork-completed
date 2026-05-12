import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComplianceCheckService, ComplianceResponseDTO, ComplianceRequestDTO } from '../../core/services/compliance-check.service';
import { InspectionService, InspectionResponseDTO } from '../../core/services/inspection.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-compliance-check-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './compliance-check-list.component.html',
  styleUrl: './compliance-check-list.component.css'
})
export class ComplianceCheckListComponent implements OnInit {
  private complianceService = inject(ComplianceCheckService);
  private inspectionService = inject(InspectionService);
  private auth = inject(AuthService);

  search = signal('');
  filterResult = signal('');
  filterStatus = signal('');
  showForm = signal(false);
  form: Partial<ComplianceRequestDTO> = {};
  editingCheckId: number | null = null;

  complianceChecks = signal<ComplianceResponseDTO[]>([]);
  inspections = signal<InspectionResponseDTO[]>([]);

  canAdd = computed(() => this.auth.hasRole('Safety Officer', 'Administrator', 'Hazard Officer'));
  canDelete = computed(() => this.auth.hasRole('Safety Officer', 'Administrator'));

  ngOnInit() {
    this.loadChecks();
    this.loadInspections();
  }

  async loadChecks() {
    try {
      const data = await this.complianceService.getAllChecks();
      this.complianceChecks.set(data || []);
    } catch (e) {
      console.error('Failed to load compliance checks', e);
    }
  }

  async loadInspections() {
    try {
      const data = await this.inspectionService.getAllInspections();
      this.inspections.set(data || []);
    } catch (e) {
      console.error('Failed to load inspections', e);
    }
  }

  pass = computed(() => this.complianceChecks().filter(c => c.complianceCheckResult === 'PASS').length);
  partial = computed(() => this.complianceChecks().filter(c => c.complianceCheckResult === 'PARTIAL').length);
  fail = computed(() => this.complianceChecks().filter(c => c.complianceCheckResult === 'FAIL').length);

  filtered = computed(() => {
    let list = this.complianceChecks();
    const q = this.search().toLowerCase();
    const res = this.filterResult();
    const stat = this.filterStatus();
    if (q) list = list.filter(c => c.complianceCheckNotes?.toLowerCase().includes(q) || String(c.inspectionId).includes(q));
    if (res) list = list.filter(c => c.complianceCheckResult === res);
    if (stat) list = list.filter(c => c.complianceCheckStatus === stat);
    return list;
  });

  openForm(): void {
    this.form = { complianceCheckResult: 'PASS', complianceCheckStatus: 'OPEN', complianceCheckDate: new Date().toISOString().split('T')[0], inspectionId: null as any };
    this.editingCheckId = null;
    this.showForm.set(true);
  }

  editCheck(c: ComplianceResponseDTO): void {
    this.form = { ...c };
    if (this.form.complianceCheckDate) {
       this.form.complianceCheckDate = new Date(this.form.complianceCheckDate).toISOString().split('T')[0];
    }
    this.editingCheckId = c.checkId;
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); }

  async save() {
    if (!this.form.inspectionId || !this.form.complianceCheckDate || !this.form.complianceCheckResult) return;
    try {
      const dto = this.form as ComplianceRequestDTO;
      if (this.editingCheckId) {
        await this.complianceService.updateCheck(this.editingCheckId, dto);
      } else {
        await this.complianceService.createCheck(dto);
      }
      this.closeForm();
      this.loadChecks();
    } catch (e) {
      console.error('Failed to save compliance check', e);
    }
  }

  async deleteCheck(id: number) {
    if (confirm('Are you sure you want to delete this compliance check?')) {
      try {
        await this.complianceService.deleteCheck(id);
        this.loadChecks();
      } catch (e) {
        console.error('Failed to delete compliance check', e);
      }
    }
  }

  resultBadge(r: string): string {
    const map: Record<string, string> = { PASS: 'badge badge-success', PARTIAL: 'badge badge-warning', FAIL: 'badge badge-danger' };
    return map[r] ?? 'badge badge-neutral';
  }
  
  statusBadge(s: string): string {
    return s === 'CLOSED' ? 'badge badge-neutral' : 'badge badge-warning';
  }

  formatResult(s: string): string {
    if (!s) return '';
    return s.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }
}
