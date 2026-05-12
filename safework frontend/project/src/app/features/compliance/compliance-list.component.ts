import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComplianceRecordService } from '../../core/services/compliance-record.service';
import { AuditService } from '../../core/services/audit.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-compliance-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './compliance-list.component.html',
  styleUrl: './compliance-list.component.css'
})
export class ComplianceListComponent implements OnInit {
  private complianceService = inject(ComplianceRecordService);
  private auditService = inject(AuditService);
  private auth = inject(AuthService);

  search = signal('');
  filterResult = signal('');
  filterType = signal('');
  showForm = signal(false);
  form: any = {};//its store as obj key and value
  editingRecordId: number | null = null;

  records = signal<any[]>([]);

  canAdd = computed(() => this.auth.hasRole('Compliance Officer', 'Administrator'));

  ngOnInit() {
    this.loadRecords();
  }

  async loadRecords() {
    try {
      const data = await this.complianceService.getAllComplianceRecords();
      this.records.set(data || []);
    } catch (e) {
      console.error(e);
    }
  }



  filtered = computed(() => {
    let list = this.records();
    const q = this.search().toLowerCase();
    const res = this.filterResult();
    const type = this.filterType();
    if (q) list = list.filter(c => c.entityType?.toLowerCase().includes(q) || c.complianceNotes?.toLowerCase().includes(q) || c.entityId?.toString().includes(q));
    if (res) list = list.filter(c => c.complianceResult === res);
    if (type) list = list.filter(c => c.entityType === type);
    return list;
  });

  //calculated all compliance statistics for the summary cards
  pass = computed(() => this.records().filter(c => c.complianceResult === 'COMPLIANT').length);
  partial = computed(() => this.records().filter(c => c.complianceResult === 'PARTIALLY_COMPLIANT').length);
  fail = computed(() => this.records().filter(c => c.complianceResult === 'NON_COMPLIANT').length);
  rate = computed(() => {
    const t = this.records().length;
    return t ? Math.round((this.pass() / t) * 100) : 0;
  });

  openForm(): void {
    this.form = { complianceResult: 'COMPLIANT', entityType: 'Inspection', complianceDate: new Date().toISOString().split('T')[0], entityId: null };
    this.editingRecordId = null;//not editing, creating new
    this.showForm.set(true);// open the form modal
  }

  editRecord(c: any): void {
    this.form = { ...c };
    if (this.form.complianceDate) {
      this.form.complianceDate = new Date(this.form.complianceDate).toISOString().split('T')[0];
    }
    this.editingRecordId = c.complianceId;// set the id of the record being edited
    this.showForm.set(true);// open the form modal
  }

  closeForm(): void { 
    this.showForm.set(false);// close the form modal
   }

  async save() {
    if (!this.form.entityId) return;
    try {
      if (this.editingRecordId) {
        await this.complianceService.updateComplianceRecord(this.editingRecordId, this.form);
      } else {
        await this.complianceService.createComplianceRecord(this.form);
      }
      this.closeForm();
      this.loadRecords();
    } catch (e) {
      console.error('Failed to save compliance record', e);
    }
  }

  async deleteRecord(id: number) {
    if (confirm('Are you sure you want to delete this record?')) {
      try {
        await this.complianceService.deleteComplianceRecord(id);
        this.loadRecords();
      } catch (e) {
        console.error('Failed to delete compliance record', e);
      }
    }
  }

  //according to result apply color on  output text
  resultBadge(r: string): string {
    const map: Record<string, string> = { COMPLIANT: 'badge badge-success', NON_COMPLIANT: 'badge badge-danger', PARTIALLY_COMPLIANT: 'badge badge-warning', NOT_APPLICABLE: 'badge badge-neutral' };
    return map[r] ?? 'badge badge-neutral';
  }

  //format result string to be more human readable, e.g. COMPLIANT -> Compliant,
  formatResult(s: string): string {
    if (!s) return '';
    return s.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }
}
