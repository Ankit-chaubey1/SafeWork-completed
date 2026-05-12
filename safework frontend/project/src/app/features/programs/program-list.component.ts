import { Component, computed, signal, inject } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MockDataService } from '../../core/services/mock-data.service';
import { AuthService } from '../../core/services/auth.service';
import { Program } from '../../core/models';

@Component({
  selector: 'app-program-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './program-list.component.html',
  styles: [`
    .programs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
    .program-card { padding: 20px; display: flex; flex-direction: column; gap: 12px; }
    .program-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
    .program-title { font-size: 15px; font-weight: 600; line-height: 1.3; }
    .program-dates { margin-top: 2px; }
    .program-desc { font-size: 13px; line-height: 1.5; }
    .program-progress { display: flex; flex-direction: column; gap: 4px; }
    .progress-info { display: flex; justify-content: space-between; }
    .progress-bar-bg { height: 6px; background: var(--border); border-radius: 4px; overflow: hidden; }
    .progress-bar-fill { height: 100%; background: var(--secondary); border-radius: 4px; transition: width .3s; }
    .program-footer { display: flex; align-items: center; justify-content: space-between; padding-top: 4px; border-top: 1px solid var(--border); }
    .fw-600 { font-weight: 600; }
    .detail-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:8px}
    .detail-item{display:flex;flex-direction:column;gap:3px}
    .detail-label{font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:.04em;color:var(--text-muted)}
    .text-xs { font-size: 11px; }
  `],
})
export class ProgramListComponent {
  private data = inject(MockDataService);
  private auth = inject(AuthService);

  search = signal('');
  filterStatus = signal('');
  showForm = signal(false);
  selected = signal<Program | null>(null);
  editing = signal<Program | null>(null);
  operationError = this.data.operationError;
  form: Partial<Program> = {};

  canAdd = computed(() => this.auth.hasRole('Manager', 'Administrator'));

  filtered = computed(() => {
    let list = this.data.programs();
    const q = this.search().toLowerCase();
    const status = this.filterStatus();
    if (q) {
      list = list.filter(p => 
        p.title.toLowerCase().includes(q) || 
        p.description.toLowerCase().includes(q) ||
        p.programId.toString().includes(q)
      );
    }
    if (status) list = list.filter(p => p.status === status);
    return list;
  });

  getStats(p: Program) {
    const trainings = this.data.trainings().filter(t => t.programId === p.programId);
    const enrolled = trainings.length;
    const completed = trainings.filter(t => t.status === 'COMPLETED').length;
    const inProgress = trainings.filter(t => t.status === 'IN_PROGRESS' || t.status === 'NOT_STARTED').length;
    const rate = enrolled ? Math.round((completed / enrolled) * 100) : 0;
    return { enrolled, completed, inProgress, rate };
  }

  openForm(): void {
    this.editing.set(null);
    this.form = { status: 'Planned', enrolledCount: 0, completedCount: 0 };
    this.showForm.set(true);
  }

  editProgram(p: Program): void {
    this.editing.set(p);
    this.form = { ...p };
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editing.set(null);
  }

  save(): void {
    if (!this.form.title || !this.form.startDate || !this.form.endDate) return;
    const payload = { ...this.form } as Program;
    if (this.editing()?.programId) {
      payload.programId = this.editing()!.programId;
      this.data.updateProgram(payload);
    } else {
      this.data.addProgram({ ...payload, programId: '' });
    }
    this.closeForm();
  }

  deleteProgram(p: Program): void {
    this.data.deleteProgram(p.programId);
    if (this.selected()?.programId === p.programId) this.selected.set(null);
  }

  statusBadge(s: string): string {
    const map: Record<string, string> = { Active: 'badge badge-success', Planned: 'badge badge-primary', Completed: 'badge badge-neutral', Cancelled: 'badge badge-danger' };
    return map[s] ?? 'badge badge-neutral';
  }
}
