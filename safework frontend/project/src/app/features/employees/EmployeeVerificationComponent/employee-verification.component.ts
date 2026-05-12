import { Component, computed, inject } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { MockDataService } from '../../../core/services/mock-data.service';
import { Employee } from '../../../core/models';

@Component({
  selector: 'app-employee-verification',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './employee-verification.component.html',
  styles: [`.avatar{width:34px;height:34px;border-radius:50%;background:var(--primary-lighter);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;flex-shrink:0}.fw-600{font-weight:600}`],
})
export class EmployeeVerificationComponent {
  private data = inject(MockDataService);
  error = this.data.operationError;

  pendingEmployees = computed(() => this.data.employees().filter(e => e.status === 'Pending'));

  approve(employee: Employee): void {
    this.data.approveEmployee(employee.employeeId);
  }

  initials(name: string): string {
    return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }
}
