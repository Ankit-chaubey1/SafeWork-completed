import { Component, computed, inject } from '@angular/core';
import { NgFor, NgIf, DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MockDataService } from '../../core/services/mock-data.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, NgClass, FormsModule],
  templateUrl: './notifications.component.html',
  styles: [`
    .notif-list { display: flex; flex-direction: column; gap: 8px; }
    .notif-item { display: flex; align-items: flex-start; gap: 12px; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 14px 16px; cursor: pointer; transition: all .15s; }
    .notif-item:hover { border-color: var(--border-dark); box-shadow: var(--shadow); }
    .notif-item.unread { border-left: 3px solid var(--primary-light); background: #f8fafd; }
    .notif-dot-wrap { display: flex; align-items: center; padding-top: 2px; }
    .notif-dot-indicator { width: 8px; height: 8px; border-radius: 50%; background: var(--border-dark); flex-shrink: 0; }
    .notif-dot-indicator.active { background: var(--primary-light); }
    .notif-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
    .notif-body { flex: 1; }
    .notif-msg { font-size: 13.5px; line-height: 1.5; margin-bottom: 6px; }
    .notif-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .unread-pill { background: var(--primary); color: #fff; font-size: 10px; font-weight: 700; padding: 2px 8px; border-radius: 10px; white-space: nowrap; align-self: center; }
  `],
})
export class NotificationsComponent {
  private data = inject(MockDataService);
  private auth = inject(AuthService);

  filterCat = '';

  all = computed(() => this.data.getNotificationsForUser(this.auth.currentUser()?.userId ?? ''));
  unread = computed(() => this.all().filter(n => n.status === 'Unread').length);

  filtered = computed(() => {
    let list = this.all();
    if (this.filterCat) list = list.filter(n => n.category === this.filterCat);
    return list.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());
  });

  read(id: string): void { this.data.markNotificationRead(id); }

  markAllRead(): void {
    this.all().forEach(n => this.data.markNotificationRead(n.notificationId));
  }

  catIcon(cat: string): string {
    return { Hazard: '⚠', Inspection: '🔍', Program: '🎓', Compliance: '📊' }[cat] ?? '🔔';
  }

  catColor(cat: string): string {
    return { Hazard: '#fee2e2', Inspection: '#dbeafe', Program: '#d1fae5', Compliance: '#fef3c7' }[cat] ?? '#f1f5f9';
  }

  catBadge(cat: string): string {
    const map: Record<string, string> = { Hazard: 'badge badge-danger', Inspection: 'badge badge-info', Program: 'badge badge-success', Compliance: 'badge badge-warning' };
    return map[cat] ?? 'badge badge-neutral';
  }
}
