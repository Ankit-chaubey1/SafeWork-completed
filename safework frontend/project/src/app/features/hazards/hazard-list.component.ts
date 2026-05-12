import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HazardService, HazardReportProjection, HazardRequestDto } from '../../core/services/hazard.service';
import { AuthService } from '../../core/services/auth.service';
import { IncidentService, IncidentReportProjection } from '../../core/services/incident.service';

@Component({
  selector: 'app-hazard-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './hazard-list.component.html',
  styleUrl: './hazard-list.component.css'
})
export class HazardListComponent implements OnInit {
  private hazardService = inject(HazardService);
  private incidentService = inject(IncidentService);
  private auth = inject(AuthService);

  search = signal('');
  filterStatus = signal('');
  showForm = signal(false);
  selected = signal<HazardReportProjection | null>(null);
  relatedIncident = signal<IncidentReportProjection | null>(null);
  loadingIncident = signal<boolean>(false);
  statusTarget = signal<HazardReportProjection | null>(null);
  newStatus: string = 'PENDING';
  form: Partial<HazardRequestDto> = {};

  hazards = signal<HazardReportProjection[]>([]);

  canUpdateStatus = computed(() => this.auth.hasRole('Safety Officer', 'Hazard Officer', 'Manager', 'Administrator'));

  filtered = computed(() => {
    let list = this.hazards();
    const q = this.search().toLowerCase();
    const status = this.filterStatus();
    if (q) {
      list = list.filter(h => 
        h.hazardDescription?.toLowerCase().includes(q) || 
        h.hazardLocation?.toLowerCase().includes(q) ||
        h.hazardId.toString().includes(q)
      );
    }
    if (status) list = list.filter(h => h.hazardStatus === status);
    return list;
  });

  summaryStats = computed(() => {
    const h = this.hazards();
    return [
      { status: 'PENDING', count: h.filter(x => x.hazardStatus === 'PENDING').length, cls: 'badge badge-warning' },
      { status: 'COMPLETED', count: h.filter(x => x.hazardStatus === 'COMPLETED').length, cls: 'badge badge-success' },
    ];
  });

  ngOnInit() {
    this.loadHazards();
  }

  async loadHazards() {
    try {
      if (this.auth.hasRole('Employee')) {
        const uid = Number(this.auth.currentUser()?.userId ?? 0);
        const data = await this.hazardService.getHazardsByEmployee(uid);
        this.hazards.set(data || []);
      } else {
        const data = await this.hazardService.getAllHazards();
        this.hazards.set(data || []);
      }
    } catch (error) {
      console.error('Failed to load hazards', error);
    }
  }

  openForm(): void {
    this.form = { hazardStatus: 'PENDING' };
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); }

  async save() {
    if (!this.form.hazardDescription || !this.form.hazardLocation) return;
    try {
      const dto: HazardRequestDto = {
        hazardDescription: this.form.hazardDescription,
        hazardLocation: this.form.hazardLocation,
        hazardStatus: 'PENDING',
        employeeId: Number(this.auth.currentUser()?.userId ?? 0)
      };
      await this.hazardService.addHazard(dto);
      this.closeForm();
      this.loadHazards();
    } catch (error) {
      console.error('Failed to save hazard', error);
    }
  }

  async viewDetail(h: HazardReportProjection) {
    this.selected.set(h);
    this.relatedIncident.set(null);
    if (h.hazardStatus === 'COMPLETED') {
       this.loadingIncident.set(true);
       try {
         const incident = await this.incidentService.getIncidentByHazardId(h.hazardId);
         this.relatedIncident.set(incident);
       } catch (error) {
         console.warn('No incident found or failed to load incident details', error);
       } finally {
         this.loadingIncident.set(false);
       }
    }
  }

  openStatusModal(h: HazardReportProjection): void {
    this.statusTarget.set(h);
    this.newStatus = h.hazardStatus || 'PENDING';
  }

  async applyStatus() {
    const h = this.statusTarget();
    if (!h) return;
    try {
      const dto: HazardRequestDto = {
        employeeId: h.employeeId,
        hazardDescription: h.hazardDescription,
        hazardLocation: h.hazardLocation,
        hazardStatus: this.newStatus
      };
      await this.hazardService.updateHazard(h.hazardId, dto);
      this.statusTarget.set(null);
      this.loadHazards();
    } catch (error) {
      console.error('Failed to update hazard', error);
    }
  }

  async deleteHazard(hazardId: number) {
    if (confirm('Are you sure you want to delete this hazard?')) {
      try {
        await this.hazardService.deleteHazard(hazardId);
        this.loadHazards();
      } catch (error) {
        console.error('Failed to delete hazard', error);
      }
    }
  }

  statusBadge(s: string): string {
    const map: Record<string, string> = { PENDING: 'badge badge-warning', COMPLETED: 'badge badge-success' };
    return map[s] ?? 'badge badge-neutral';
  }
}

