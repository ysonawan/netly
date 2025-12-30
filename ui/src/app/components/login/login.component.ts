import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    standalone: false
})
export class LoginComponent {
  loginForm: FormGroup;
  otpForm: FormGroup;
  loading = false;
  sessionExpiredMessage = '';
  returnUrl: string;
  loginMode: 'password' | 'otp' = 'password';
  otpSent = false;
  otpSending = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }

    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.otpForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

    // Check if user was redirected due to session expiration
    if (this.route.snapshot.queryParams['sessionExpired'] === 'true') {
      this.sessionExpiredMessage = 'Your session has expired. Please login again.';
    }
  }

  get f() {
    return this.loginForm.controls;
  }

  get otpF() {
    return this.otpForm.controls;
  }

  switchLoginMode(mode: 'password' | 'otp'): void {
    this.loginMode = mode;
    this.sessionExpiredMessage = '';
    this.otpSent = false;

    // Reset forms
    this.loginForm.reset();
    this.otpForm.reset();
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.sessionExpiredMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigate([this.returnUrl]);
      },
      error: () => {
        this.toastr.error('Invalid email or password');
        this.loading = false;
      }
    });
  }

  requestOtp(): void {
    const email = this.otpForm.get('email')?.value;
    if (!email || this.otpForm.get('email')?.invalid) {
      this.otpForm.get('email')?.markAsTouched();
      return;
    }

    this.otpSending = true;

    this.authService.requestOtp(email).subscribe({
      next: () => {
        this.otpSent = true;
        this.otpSending = false;
        this.toastr.success('OTP sent to your email. Please check your inbox.');
      },
      error: () => {
        this.toastr.error('Failed to send OTP. Please check your email and try again.');
        this.otpSending = false;
      }
    });
  }

  verifyOtp(): void {
    if (this.otpForm.invalid) {
      Object.keys(this.otpForm.controls).forEach(key => {
        this.otpForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading = true;

    this.authService.verifyOtp(this.otpForm.value).subscribe({
      next: () => {
        this.router.navigate([this.returnUrl]);
      },
      error: () => {
        this.toastr.error('Invalid or expired OTP. Please try again.');
        this.loading = false;
      }
    });
  }
}

