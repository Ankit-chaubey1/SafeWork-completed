import { Component, computed, inject } from '@angular/core';
import { NgFor, NgIf, DatePipe, KeyValuePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MockDataService } from '../../core/services/mock-data.service';
import { Report } from '../../core/models';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, KeyValuePipe, FormsModule],
 templateUrl: './reports.component.html',
  styles: [`
    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    @media (max-width: 900px) { .grid-2 { grid-template-columns: 1fr; } }
    .chart-row { display: flex; align-items: center; gap: 12px; }
    .chart-label { width: 100px; font-size: 13px; font-weight: 500; flex-shrink: 0; }
    .chart-bar-outer { flex: 1; height: 10px; background: var(--border); border-radius: 6px; overflow: hidden; }
    .chart-bar-inner { height: 100%; border-radius: 6px; transition: width .4s; }
    .chart-val { width: 32px; text-align: right; font-size: 13px; font-weight: 600; }
    .report-row { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-bottom: 1px solid var(--border); cursor: pointer; transition: background .15s; }
    .report-row:hover { background: var(--bg); }
    .report-row:last-of-type { border-bottom: none; }
    .report-title { font-size: 14px; font-weight: 600; margin-bottom: 2px; }
    .expand-icon { color: var(--text-muted); font-size: 11px; }
    .report-detail { background: var(--bg); padding: 20px; border-top: 1px solid var(--border); }
    .metrics-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
    .metric-item { background: var(--surface); border-radius: var(--radius); padding: 12px; border: 1px solid var(--border); }
    .metric-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: .04em; color: var(--text-muted); margin-bottom: 4px; }
    .metric-value { font-size: 1.25rem; font-weight: 700; color: var(--text); }
  `],
})
export class ReportsComponent {
  private data = inject(MockDataService);
  filterScope = '';
  selected: Report | null = null;

  totalHazards = computed(() => this.data.hazards().length);
  openHazards = computed(() => this.data.hazards().filter(h => h.status === 'Open' || h.status === 'Under Investigation').length);
  resolvedHazards = computed(() => this.data.hazards().filter(h => h.status === 'Resolved').length);
  totalInspections = computed(() => this.data.inspections().length);
  completedInspections = computed(() => this.data.inspections().filter(i => i.status === 'Completed').length);
  totalCompliance = computed(() => this.data.complianceRecords().length);
  compliantCount = computed(() => this.data.complianceRecords().filter(c => c.result === 'Compliant').length);
  complianceRate = computed(() => {
    const t = this.totalCompliance();
    return t ? Math.round((this.compliantCount() / t) * 100) : 0;
  });
  totalTrainings = computed(() => this.data.trainings().length);
  completedTrainings = computed(() => this.data.trainings().filter(t => t.status === 'COMPLETED').length);
  trainingRate = computed(() => {
    const t = this.totalTrainings();
    return t ? Math.round((this.completedTrainings() / t) * 100) : 0;
  });

  hazardBySeverity = computed(() => {
    const h = this.data.hazards();
    const max = Math.max(h.length, 1);
    const colors: Record<string, string> = { Critical: '#dc2626', High: '#f59e0b', Medium: '#0284c7', Low: '#059669' };
    return ['Critical', 'High', 'Medium', 'Low'].map(s => ({
      label: s, count: h.filter(x => x.severity === s).length,
      pct: (h.filter(x => x.severity === s).length / max) * 100,
      color: colors[s],
    }));
  });

  complianceByType = computed(() => {
    const records = this.data.complianceRecords();
    return ['Hazard', 'Inspection', 'Program'].map(type => {
      const items = records.filter(c => c.type === type);
      const rate = items.length ? Math.round((items.filter(c => c.result === 'Compliant').length / items.length) * 100) : 0;
      return { label: type, rate, pct: rate };
    });
  });

  filteredReports = computed(() => {
    let list = this.data.reports();
    if (this.filterScope) list = list.filter(r => r.scope === this.filterScope);
    return list;
  });

  formatKey(key: string): string {
    return key.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase());
  }
}
