import { Component, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MockDataService } from '../../core/services/mock-data.service';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, FormsModule],
 templateUrl: './audit-log.component.html',
 styleUrl: './audit-log.component.css'
})

export class AuditLogComponent {
  private data = inject(MockDataService);
  search = signal('');
  filterAction = signal('');
  dateFrom = signal('');
  dateTo = signal('');
  selectedLog: any = null;

  filtered = computed(() => {
    let list = [...this.data.auditLogs()].sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
    const q = this.search().toLowerCase();
    const action = this.filterAction();
    const fromDate = this.dateFrom();
    const toDate = this.dateTo();
    
    if (q) {
      list = list.filter(l => 
        l.userName.toLowerCase().includes(q) || 
        l.resource.toLowerCase().includes(q) || 
        l.action.toLowerCase().includes(q)
      );
    }
    
    if (action) {
      list = list.filter(l => l.action === action);
    }

    if (fromDate) {
      const from = new Date(fromDate).getTime();
      list = list.filter(l => new Date(l.timestamp).getTime() >= from);
    }

    if (toDate) {
      const to = new Date(toDate).setHours(23, 59, 59, 999);
      list = list.filter(l => new Date(l.timestamp).getTime() <= to);
    }

    return list;
  });

  actionBadge(a: string): string {
    const map: Record<string, string> = { CREATE: 'badge badge-success', UPDATE: 'badge badge-info', DELETE: 'badge badge-danger', LOGIN: 'badge badge-neutral' };
    return map[a] ?? 'badge badge-neutral';
  }

  viewDetails(log: any) {
    this.selectedLog = log;
  }

  clearLogs() {
    if (confirm('Are you sure you want to clear all audit logs? This action cannot be undone.')) {
      this.data.auditLogs.set([]);
    }
  }

  exportToCSV() {
    const data = this.filtered();
    if (data.length === 0) return;

    const headers = ['Action', 'Resource', 'User', 'Timestamp'];
    const csvRows = [headers.join(',')];

    for (const row of data) {
      const values = [
        row.action,
        `"${row.resource.replace(/"/g, '""')}"`,
        row.userName,
        row.timestamp
      ];
      csvRows.push(values.join(','));
    }

    const blob = new Blob([csvRows.join('\n')], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-log-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
