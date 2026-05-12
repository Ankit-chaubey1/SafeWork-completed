import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IncidentService, IncidentReportProjection, IncidentRequestDto } from '../../core/services/incident.service';
import { HazardService, HazardReportProjection } from '../../core/services/hazard.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [NgFor, NgIf, DatePipe, SlicePipe, FormsModule],
  templateUrl: './incident-list.component.html',
  styleUrl: './incident-list.component.css'
})
export class IncidentListComponent implements OnInit {
  private incidentService = inject(IncidentService);
  private hazardService = inject(HazardService);
  private auth = inject(AuthService);

  search = signal('');
  selected = signal<IncidentReportProjection | null>(null);
  showForm = signal(false);
  editingIncidentId: number | null = null;
  
  form = {
    hazardId: null as number | null,
    action: ''
  };

  incidents = signal<IncidentReportProjection[]>([]);
  availableHazards = signal<HazardReportProjection[]>([]);

  canManage = computed(() => this.auth.hasRole('Safety Officer', 'Hazard Officer', 'Administrator'));

  ngOnInit() {
    this.loadIncidents();
  }

  async loadIncidents() {
    try {
      const data = await this.incidentService.getAllIncidents();
      this.incidents.set(data || []);
    } catch (error) {
      console.error('Failed to load incidents', error);
    }
  }

  async loadAvailableHazards() {
    try {
      const allHazards = await this.hazardService.getAllHazards();
      // Filter out hazards that already have an incident
      const existingIncidentHazardIds = new Set(this.incidents().map(i => i.hazardId));
      this.availableHazards.set(allHazards.filter(h => !existingIncidentHazardIds.has(h.hazardId)));
    } catch (error) {
      console.error('Failed to load hazards', error);
    }
  }

  filtered = computed(() => {
    let list = this.incidents();
    const q = this.search().toLowerCase();
    if (q) {
      list = list.filter(i => 
        i.hazardDescription?.toLowerCase().includes(q) || 
        i.action?.toLowerCase().includes(q) ||
        i.incidentId.toString().includes(q) ||
        i.hazardId.toString().includes(q)
      );
    }
    return list;
  });

  async openForm() {
    this.form = { hazardId: null, action: '' };
    this.editingIncidentId = null;
    await this.loadAvailableHazards();
    this.showForm.set(true);
  }

  editIncident(incident: IncidentReportProjection) {
    this.editingIncidentId = incident.incidentId;
    this.form = { hazardId: incident.hazardId, action: incident.action };
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
  }

  async save() {
    if (!this.form.action) return;
    
    try {
      const dto: IncidentRequestDto = { action: this.form.action };
      
      if (this.editingIncidentId) {
        await this.incidentService.updateIncident(this.editingIncidentId, dto);
      } else {
        if (!this.form.hazardId) return;
        await this.incidentService.addIncident(this.form.hazardId, dto);
      }
      
      this.closeForm();
      this.loadIncidents();
    } catch (error) {
      console.error('Failed to save incident', error);
    }
  }

  async deleteIncident(incidentId: number) {
    if (confirm('Are you sure you want to delete this incident? The associated hazard will revert to PENDING status.')) {
      try {
        await this.incidentService.deleteIncident(incidentId);
        this.loadIncidents();
      } catch (error) {
        console.error('Failed to delete incident', error);
      }
    }
  }
}


