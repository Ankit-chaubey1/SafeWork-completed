import { Component, computed, signal, inject } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../core/services/audit.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './audit-list.component.html',
  styleUrl: './audit-list.component.css'
})
export class AuditListComponent {
  private auditService = inject(AuditService);
  private auth = inject(AuthService);

  search = signal('');
  filterStatus = signal('');
  showForm = signal(false);
  selected = signal<any | null>(null);
  form: any = {};
  editingAuditId: number | null = null;
  audits = signal<any[]>([]);

  canAdd = computed(() => this.auth.hasRole('Compliance Officer', 'Administrator'));

  ngOnInit() {
    this.loadAudits();
  }

  async loadAudits() {
    try {
      const data = await this.auditService.getAllAudits();
      this.audits.set(data || []);
    } catch (e) {
      console.error(e);
    }
  }

  filtered = computed(() => {
    let list = this.audits();
    const q = this.search().toLowerCase();
    const status = this.filterStatus();
    if (q) list = list.filter(a => a.auditTitle?.toLowerCase().includes(q) || a.auditFinding?.toLowerCase().includes(q));
    if (status) list = list.filter(a => a.auditStatus === status);
    return list;
  });

  openForm(): void {
    this.form = { auditStatus: 'Pending', auditScope: 'SECTIONAL', auditDate: new Date().toISOString().split('T')[0] };
    this.editingAuditId = null;
    this.showForm.set(true);
  }

  editAudit(a: any): void {
    this.form = { ...a };
    if (this.form.auditDate) {
      this.form.auditDate = new Date(this.form.auditDate).toISOString().split('T')[0];
    }
    this.editingAuditId = a.auditId;
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); }

  async save() {
    if (!this.form.auditTitle || !this.form.auditScope || !this.form.auditDate) return;
    try {
      if (this.editingAuditId) {
        await this.auditService.updateAudit(this.editingAuditId, this.form);
      } else {
        await this.auditService.createAudit(this.form);
      }
      this.closeForm();
      this.loadAudits();
    } catch (e) {
      console.error('Failed to save audit', e);
    }
  }

  async deleteAudit(id: number) {
    if (confirm('Are you sure you want to delete this audit?')) {
      try {
        await this.auditService.deleteAudit(id);
        this.loadAudits();
      } catch (e) {
        console.error('Failed to delete audit', e);
      }
    }
  }

  statusBadge(s: string): string {
    const map: Record<string, string> = { Pending: 'badge badge-primary', Open: 'badge badge-warning', Closed: 'badge badge-success' };
    return map[s] ?? 'badge badge-neutral';
  }

  formatStatus(s: string): string {
    if (!s) return '';
    return s.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }
}
