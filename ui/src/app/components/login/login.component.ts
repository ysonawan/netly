import {AfterViewInit, Component, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';
declare const google: any;
@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    standalone: false
})
export class LoginComponent implements OnInit, AfterViewInit{
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

  ngOnInit(): void {

  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.renderGoogleButton();
    }, 10);
  }

  renderGoogleButton() {
    const el = document.getElementById("google-btn");
    if (!el) return;
    // ✅ Clear old button (important after navigation)
    el.innerHTML = '';
    this.authService.getGoogleClientId().subscribe({
      next: (response) => {
        google.accounts.id.initialize({
          client_id: response.googleClientId,
          callback: (response: any) => this.handleGoogleLogin(response)
        });
        google.accounts.id.renderButton(
            document.getElementById("google-btn"), {theme: "outline", size: "large"}
        );
      },
      error: (error) => {
        if(error.error.message) {
          this.toastr.error(error.error.message, 'Error');
        } else {
          console.error("Failed to load Google Client ID", error);
        }
      }
    });
  }

  handleGoogleLogin(response: any) {
    const idToken = response.credential;
    this.authService.googleAuth(idToken).subscribe({
      next: (response) => {
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        if(error.error.message) {
          this.toastr.error(error.error.message, 'Error');
        } else {
          this.toastr.error('An unexpected error occurred during Google login. Please different login option.', 'Error');
        }
      }
    });
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
        this.toastr.error('Invalid email or password', 'Login Failed');
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
        this.toastr.success('OTP sent to your email. Please check your inbox.', 'Success');
      },
      error: () => {
        this.toastr.error('Failed to send OTP. Please check your email and try again.', 'Error');
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
        this.toastr.error('Invalid or expired OTP. Please try again.', 'Verification Failed');
        this.loading = false;
      }
    });
  }
}

