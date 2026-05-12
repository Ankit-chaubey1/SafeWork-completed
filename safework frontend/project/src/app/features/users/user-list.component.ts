import { Component, computed, signal, inject, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, UserPublicDTO, UserRegistrationDTO, UserUpdateDTO } from '../../core/services/user.service';

const ROLES = ['EMPLOYEE', 'SAFETY_OFFICER', 'HAZARD_OFFICER', 'ADMIN', 'COMPLIANCE_OFFICER'];

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [NgFor, NgIf, FormsModule],
  templateUrl: './user-list.component.html',
  styles: [`.avatar{width:34px;height:34px;border-radius:50%;background:var(--primary-lighter);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;flex-shrink:0}.fw-600{font-weight:600}.text-danger{color: var(--danger)}`],
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);

  search = signal('');
  filterRole = signal('');
  filterStatus = signal('');
  roles = ROLES;
  showForm = signal(false);
  editing = signal<UserPublicDTO | null>(null);
  operationError = signal<string | null>(null);
  form: any = {};

  users = signal<UserPublicDTO[]>([]);

  filtered = computed(() => {
    let list = this.users();
    const q = this.search().toLowerCase();
    const role = this.filterRole();
    const status = this.filterStatus();
    if (q) list = list.filter(u => u.userName?.toLowerCase().includes(q) || u.userEmail?.toLowerCase().includes(q));
    if (role) list = list.filter(u => u.userRole === role);
    if (status) list = list.filter(u => u.userStatus === status);
    return list;
  });

  ngOnInit() {
    this.loadUsers();
  }

  async loadUsers() {
    try {
      const data = await this.userService.getAllUsers();
      this.users.set(data || []);
      this.operationError.set(null);
    } catch (error: any) {
      this.operationError.set(error?.error?.message || 'Failed to load users');
    }
  }

  openForm(u?: UserPublicDTO): void {
    this.editing.set(u ?? null);
    this.form = u ? { ...u, password: '' } : { userRole: 'EMPLOYEE', userStatus: 'ACTIVE', password: '' };
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); }

  async save() {
    if (!this.form.userName || !this.form.userEmail) {
      this.operationError.set('Name and Email are required.');
      return;
    }
    
    try {
      if (this.editing()?.userId) {
        const dto: UserUpdateDTO = {
          userName: this.form.userName,
          userEmail: this.form.userEmail,
          userContact: this.form.userContact,
          userRole: this.form.userRole,
          userStatus: this.form.userStatus
        };
        if (this.form.password) dto.password = this.form.password;
        
        await this.userService.updateUser(this.editing()!.userId, dto);
      } else {
        if (!this.form.password) {
          this.operationError.set('Password is required for new users.');
          return;
        }
        const dto: UserRegistrationDTO = {
          userName: this.form.userName,
          userEmail: this.form.userEmail,
          userContact: this.form.userContact,
          userRole: this.form.userRole,
          userStatus: this.form.userStatus,
          password: this.form.password
        };
        await this.userService.registerUser(dto);
      }
      this.closeForm();
      this.loadUsers();
    } catch (error: any) {
      this.operationError.set(error?.error?.message || 'Failed to save user.');
    }
  }

  async deleteUser(userId: number) {
    if (confirm('Are you sure you want to delete this user?')) {
      try {
        await this.userService.deleteUser(userId);
        this.loadUsers();
      } catch (error: any) {
        this.operationError.set(error?.error?.message || 'Failed to delete user.');
      }
    }
  }

  initials(name: string): string { return (name || '').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2); }

  roleBadge(r: string): string {
    const map: Record<string, string> = {
      EMPLOYEE: 'badge badge-neutral',
      SAFETY_OFFICER: 'badge badge-info',
      HAZARD_OFFICER: 'badge badge-warning',
      ADMIN: 'badge badge-primary',
      COMPLIANCE_OFFICER: 'badge badge-success'
    };
    return map[r] ?? 'badge badge-neutral';
  }
}
