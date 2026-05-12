import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NgIf, NgFor, DatePipe, NgClass, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { MockDataService } from '../../core/services/mock-data.service';
import { HazardService, HazardReportProjection } from '../../core/services/hazard.service';
import { TrainingService, Training } from '../../core/services/training.service';
import { ProgramService, Program } from '../../core/services/program.service';
import { IncidentService, IncidentReportProjection } from '../../core/services/incident.service';
import { InspectionService, InspectionResponseDTO } from '../../core/services/inspection.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe, NgClass, SlicePipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styles: [`
    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    @media (max-width: 900px) { .grid-2 { grid-template-columns: 1fr; } }
    .list-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 16px; border-bottom: 1px solid var(--border); }
    .list-row:last-child { border-bottom: none; }
    .fw-500 { font-weight: 500; font-size: 13.5px; }
    .mini-bar-wrap { width: 80px; height: 6px; background: var(--border); border-radius: 4px; overflow: hidden; }
    .mini-bar { height: 100%; background: var(--primary-light); border-radius: 4px; transition: width .3s; }
  `],
})
export class DashboardComponent implements OnInit {
  private auth = inject(AuthService);
  private data = inject(MockDataService);
  private hazardService = inject(HazardService);
  private trainingService = inject(TrainingService);
  private programService = inject(ProgramService);
  private incidentService = inject(IncidentService);
  private inspectionService = inject(InspectionService);

  today = new Date();
  user = this.auth.currentUser;
  role = this.auth.userRole;
  firstName = computed(() => this.user()?.name?.split(' ')[0] ?? '');

  allHazards = this.data.hazards;
  allIncidents = this.data.incidents;
  allInspections = this.data.inspections;
  allPrograms = this.data.programs;
  allReports = this.data.reports;
  allUsers = this.data.users;
  allEmployees = this.data.employees;
  allCompliance = this.data.complianceRecords;
  allAudits = this.data.audits;
  auditLogs = this.data.auditLogs;

  // Live Data for Employee
  empHazards = signal<HazardReportProjection[]>([]);
  empTrainings = signal<Training[]>([]);
  empPrograms = signal<Program[]>([]);

  // Live Data for Safety Officer
  soHazards = signal<HazardReportProjection[]>([]);
  soIncidents = signal<IncidentReportProjection[]>([]);
  soInspections = signal<InspectionResponseDTO[]>([]);

  ngOnInit() {
    if (this.role() === 'Employee') {
      const uid = Number(this.user()?.userId ?? 0);
      this.hazardService.getHazardsByEmployee(uid).then(res => this.empHazards.set(res || [])).catch(console.error);
      this.trainingService.getTrainingsByEmployee(uid).then(res => this.empTrainings.set(res || [])).catch(console.error);
      this.programService.getAllPrograms().then(res => this.empPrograms.set(res || [])).catch(console.error);
    } else if (this.role() === 'Safety Officer' || this.role() === 'Hazard Officer') {
      this.hazardService.getAllHazards().then(res => this.soHazards.set(res || [])).catch(console.error);
      this.incidentService.getAllIncidents().then(res => this.soIncidents.set(res || [])).catch(console.error);
      this.inspectionService.getAllInspections().then(res => this.soInspections.set(res || [])).catch(console.error);
    }
  }

  myHazards = computed(() => this.empHazards());
  myTrainings = computed(() => this.empTrainings());
  myUnread = computed(() => this.data.getUnreadCount(this.user()?.userId ?? ''));
  openHazards = computed(() => this.empHazards().filter(h => h.hazardStatus === 'PENDING').length);
  completedTrainings = computed(() => this.empTrainings().filter(t => t.trainingStatus === 'COMPLETED').length);
  trainingRate = computed(() => {
    const t = this.empTrainings();
    if (!t.length) return 0;
    return Math.round((this.completedTrainings() / t.length) * 100);
  });

  empTrainingsView = computed(() => {
    return this.empTrainings().map(t => {
      const p = this.empPrograms().find(pr => Number(pr.programId) === Number(t.programId));
      return {
        ...t,
        programTitle: p ? p.programTitle : `Program #${t.programId}`
      };
    });
  });

  openIncidents = computed(() => 0);
  criticalHazards = computed(() => this.soHazards().filter(h => h.hazardStatus === 'PENDING').length);
  highHazards = computed(() => 0);
  scheduledInspections = computed(() => this.soInspections().filter(i => i.inspectionStatus === 'SCHEDULED').length);
  completedInspections = computed(() => this.soInspections().filter(i => i.inspectionStatus === 'COMPLETED').length);
  openHazardsTotal = computed(() => this.allHazards().filter(h => h.status === 'Open' || h.status === 'Under Investigation').length);
  activePrograms = computed(() => this.allPrograms().filter(p => p.status === 'Active').length);
  plannedPrograms = computed(() => this.allPrograms().filter(p => p.status === 'Planned').length);
  totalEmployees = computed(() => this.allEmployees().length);
  activeEmployees = computed(() => this.allEmployees().filter(e => e.status === 'Active').length);
  activeUsersCount = computed(() => this.allUsers().filter(u => u.status === 'Active').length);
  overallTrainingRate = computed(() => {
    const t = this.data.trainings();
    if (!t.length) return 0;
    return Math.round((t.filter(x => x.status === 'COMPLETED').length / t.length) * 100);
  });
  compliantCount = computed(() => this.allCompliance().filter(c => c.result === 'Compliant').length);
  nonCompliantCount = computed(() => this.allCompliance().filter(c => c.result === 'Non-Compliant').length);
  complianceRate = computed(() => {
    const total = this.allCompliance().length;
    if (!total) return 0;
    return Math.round((this.compliantCount() / total) * 100);
  });
  activeAudits = computed(() => this.allAudits().filter(a => a.status === 'In Progress').length);
  userGroups = computed(() => {
    const users = this.allUsers();
    const roles = ['Employee', 'Safety Officer', 'Hazard Officer', 'Manager', 'Administrator', 'Compliance Officer', 'Government Auditor'] as const;
    const max = Math.max(...roles.map(r => users.filter(u => u.role === r).length), 1);
    return roles.map(r => ({ role: r, count: users.filter(u => u.role === r).length, pct: (users.filter(u => u.role === r).length / max) * 100 }));
  });

  private getEmployeeId(): string {
    const userId = this.user()?.userId ?? '';
    return this.allEmployees().find(e => e.userId === userId)?.employeeId ?? 'e1';
  }

  severityBadge(s: string): string {
    const map: Record<string, string> = { Critical: 'badge badge-danger', High: 'badge badge-warning', Medium: 'badge badge-info', Low: 'badge badge-neutral' };
    return map[s] ?? 'badge badge-neutral';
  }
  statusBadge(s: string): string {
    const map: Record<string, string> = { PENDING: 'badge badge-warning', COMPLETED: 'badge badge-success', Open: 'badge badge-danger', 'Under Investigation': 'badge badge-warning', Resolved: 'badge badge-success', Closed: 'badge badge-neutral' };
    return map[s] ?? 'badge badge-neutral';
  }
  getTrainingStatusLabel(s: string): string {
    const map: Record<string, string> = { COMPLETED: 'Completed', IN_PROGRESS: 'In Progress', NOT_STARTED: 'Not Started', FAILED: 'Failed', PLANNED: 'Planned' };
    return map[s] ?? s;
  }
  trainingBadge(s: string): string {
    const map: Record<string, string> = { COMPLETED: 'badge badge-success', IN_PROGRESS: 'badge badge-info', NOT_STARTED: 'badge badge-primary', FAILED: 'badge badge-danger', PLANNED: 'badge badge-warning', Completed: 'badge badge-success', 'In Progress': 'badge badge-info', 'Not Started': 'badge badge-primary', Failed: 'badge badge-danger', Planned: 'badge badge-warning' };
    return map[s] ?? 'badge badge-neutral';
  }
  incidentBadge(s: string): string {
    const map: Record<string, string> = { Open: 'badge badge-danger', 'In Progress': 'badge badge-warning', Resolved: 'badge badge-success', Closed: 'badge badge-neutral' };
    return map[s] ?? 'badge badge-neutral';
  }
  programBadge(s: string): string {
    const map: Record<string, string> = { Active: 'badge badge-success', Planned: 'badge badge-primary', Completed: 'badge badge-neutral', Cancelled: 'badge badge-danger' };
    return map[s] ?? 'badge badge-neutral';
  }
  complianceBadge(s: string): string {
    const map: Record<string, string> = { Compliant: 'badge badge-success', 'Non-Compliant': 'badge badge-danger', Partial: 'badge badge-warning' };
    return map[s] ?? 'badge badge-neutral';
  }
  auditBadge(s: string): string {
    const map: Record<string, string> = { Planned: 'badge badge-primary', 'In Progress': 'badge badge-warning', Completed: 'badge badge-success', Cancelled: 'badge badge-danger' };
    return map[s] ?? 'badge badge-neutral';
  }
}
