import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  error = signal('');
  loading = signal(false);

  features = [
    { icon: '⚠', text: 'Hazard reporting & incident management' },
    { icon: '🔍', text: 'Safety inspections & compliance tracking' },
    { icon: '📋', text: 'Workplace safety program oversight' },
    { icon: '📊', text: 'Compliance audits & analytics' },
    { icon: '🔔', text: 'Real-time alerts & notifications' },
  ];

  async submit(): Promise<void> {
    this.error.set('');
    if (!this.email || !this.password) {
      this.error.set('Please enter your email and password.');
      return;
    }
    this.loading.set(true);
    const result = await this.auth.login(this.email, this.password);
    this.loading.set(false);
    if (result.success) {
      this.router.navigate(['/dashboard']);
    } else {
      this.error.set(result.message);
    }
  }
}

