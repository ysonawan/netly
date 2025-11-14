import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../models/user.model';
import { Router } from '@angular/router';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css'],
    standalone: false
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  secondaryEmails: string[] = [];
  loading = false;
  saving = false;
  sendingReport = false;
  message: { type: 'success' | 'error', text: string } | null = null;
  editingBasic = false;
  editingPassword = false;
  basicForm = {
    name: '',
    email: ''
  };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.userService.getUserProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.secondaryEmails = profile.secondaryEmails ? [...profile.secondaryEmails] : [];
        // Start with at least 1 empty slot if there are no emails
        if (this.secondaryEmails.length === 0) {
          this.secondaryEmails.push('');
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.loading = false;
        this.showMessage('error', 'Failed to load profile');
      }
    });
  }

  addEmailSlot(): void {
    if (this.secondaryEmails.length < 10) {
      this.secondaryEmails.push('');
    }
  }

  removeEmailSlot(index: number): void {
    if (this.secondaryEmails.length > 1) {
      this.secondaryEmails.splice(index, 1);
    } else {
      // If it's the last one, just clear it instead of removing
      this.secondaryEmails[index] = '';
    }
  }

  saveSecondaryEmails(): void {
    // Filter out empty emails and validate
    const validEmails = this.secondaryEmails
      .map(email => email.trim())
      .filter(email => email !== '');

    // Check for duplicates
    const uniqueEmails = [...new Set(validEmails)];
    if (validEmails.length !== uniqueEmails.length) {
      this.showMessage('error', 'Duplicate emails are not allowed');
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    for (const email of validEmails) {
      if (!emailRegex.test(email)) {
        this.showMessage('error', `Invalid email format: ${email}`);
        return;
      }
    }

    // Check if any secondary email is the same as primary
    if (this.profile && validEmails.some(email => email.toLowerCase() === this.profile!.email.toLowerCase())) {
      this.showMessage('error', 'Secondary email cannot be the same as primary email');
      return;
    }

    this.saving = true;
    this.userService.updateSecondaryEmails({ secondaryEmails: validEmails }).subscribe({
      next: (updatedProfile) => {
        this.profile = updatedProfile;
        this.secondaryEmails = updatedProfile.secondaryEmails ? [...updatedProfile.secondaryEmails] : [];
        // Ensure at least one empty slot for adding more
        if (this.secondaryEmails.length === 0) {
          this.secondaryEmails.push('');
        }
        this.saving = false;
        this.showMessage('success', 'Secondary emails updated successfully');
      },
      error: (error) => {
        console.error('Error updating secondary emails:', error);
        this.saving = false;
        this.showMessage('error', error?.error?.message || 'Failed to update secondary emails');
      }
    });
  }

  showMessage(type: 'success' | 'error', text: string): void {
    this.message = { type, text };
    setTimeout(() => {
      this.message = null;
    }, 5000);
  }

  sendReport(): void {
    this.sendingReport = true;
    this.userService.sendPortfolioReport().subscribe({
      next: (response) => {
        this.sendingReport = false;
        this.showMessage('success', 'Portfolio report has been queued and will be sent to all your email addresses shortly.');
      },
      error: (error) => {
        console.error('Error sending report:', error);
        this.sendingReport = false;
        this.showMessage('error', error?.error?.message || 'Failed to send portfolio report. Please try again.');
      }
    });
  }

  getFilledEmailsCount(): number {
    return this.secondaryEmails.filter(email => email.trim() !== '').length;
  }

  trackByIndex(index: number): number {
    return index;
  }

  startEditBasic() {
    if (this.profile) {
      this.basicForm.name = this.profile.name;
      this.basicForm.email = this.profile.email;
      this.editingBasic = true;
    }
  }

  saveBasicInfo() {
    this.saving = true;
    const oldEmail = this.profile?.email;
    this.userService.updateBasicInfo(this.basicForm).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.editingBasic = false;
        this.saving = false;
        this.showMessage('success', 'Profile updated successfully');
        //logout user if email changed
        if (oldEmail && this.basicForm.email !== oldEmail) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
      },
      error: (err) => {
        this.saving = false;
        this.showMessage('error', err?.error?.message || 'Failed to update profile');
      }
    });
  }
}
