import { Component, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, EmployeeRegistrationRequest } from '../../../core/services/auth.service';

@Component({
  selector: 'app-employee-register',
  standalone: true,
  imports: [NgIf, NgFor, FormsModule, RouterLink],
  templateUrl: './employee-register.component.html',
  styleUrl: './employee-register.component.css'
})
export class EmployeeRegisterComponent {
  private auth = inject(AuthService);
  readonly router = inject(Router);

  loading = signal(false);
  error = signal('');
  success = signal('');
  selectedFile: File | null = null;

  genders = ['Male', 'Female', 'Other'];
  departments = ['Manufacturing', 'Safety', 'Operations', 'Compliance', 'Maintenance', 'Warehouse', 'Quality'];

  form: EmployeeRegistrationRequest = {
    userName: '',
    userEmail: '',
    userContact: '',
    password: '',
    employeeDOB: '',
    employeeGender: '',
    employeeAddress: '',
    employeeDepartmentName: '',
    employeeDocumentType: '',
    employeeFileURL: '',
  };

  /**
   * 18 vayasu aagi vitta-tha endru check seiyum logic
   */
  get isAdult(): boolean {
    if (!this.form.employeeDOB) return false;
    
    const dob = new Date(this.form.employeeDOB);
    const today = new Date();
    
    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    
    // Birthday innum varala-na oru varushathai kuraikka vendum
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
      age--;
    }
    
    return age >= 18;
  }

  /**
   * HTML Date picker-il 18 varushathuku munnadi ulla dates-ai mattum 
   * select seiya 'max' attribute-kaga ithu payanpadum
   */
  get maxAllowedDate(): string {
    const today = new Date();
    const maxDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    // Format to YYYY-MM-DD
    return maxDate.toISOString().split('T')[0];
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        this.error.set('File size exceeds 10MB limit.');
        this.selectedFile = null;
        return;
      }
      this.selectedFile = file;
      this.error.set('');
    }
  }

  async submit(regForm: NgForm): Promise<void> {
    this.error.set('');
    this.success.set('');

    // 1. Basic form validation
    if (regForm.invalid) {
      this.error.set('Please fill in all required fields correctly.');
      Object.values(regForm.controls).forEach(control => {
        control.markAsTouched();
      });
      return;
    }

    // 2. Age Validation (18+)
    if (!this.isAdult) {
      this.error.set('Employee must be at least 18 years old.');
      return;
    }

    // 3. File upload check
    if (!this.selectedFile) {
      this.error.set('A document upload is required.');
      return;
    }

    this.loading.set(true);

    const formData = new FormData();
    // Wrap JSON in a Blob for Spring Boot @RequestPart compatibility
    formData.append('employee', new Blob([JSON.stringify(this.form)], { type: 'application/json' }));
    formData.append('file', this.selectedFile);

    try {
      const result = await this.auth.registerEmployee(formData);
      this.loading.set(false);

      if (!result.success) {
        this.error.set(result.message);
        return;
      }

      this.success.set(result.message);
      setTimeout(() => this.router.navigate(['/login']), 1500);
    } catch (err) {
      this.loading.set(false);
      this.error.set('An unexpected error occurred. Please try again.');
    }
  }
}