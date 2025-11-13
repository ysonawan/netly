import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    standalone: false
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  sessionExpiredMessage = '';
  returnUrl: string;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }

    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
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

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.sessionExpiredMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigate([this.returnUrl]);
      },
      error: () => {
        this.errorMessage = 'Invalid email or password';
        this.loading = false;
      }
    });
  }
}

