import {AfterViewInit, Component, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';
declare const google: any;

@Component({
    selector: 'app-signup',
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.css'],
    standalone: false
})
export class SignupComponent implements OnInit, AfterViewInit {
  signupForm: FormGroup;
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }

    this.signupForm = this.formBuilder.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
    } else {
      confirmPassword?.setErrors(null);
    }

    return null;
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
        this.router.navigate(['/dashboard']);
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
    return this.signupForm.controls;
  }

  onSubmit(): void {
    if (this.signupForm.invalid) {
      return;
    }

    this.loading = true;

    const { name, email, password } = this.signupForm.value;

    this.authService.signup({ name, email, password }).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.toastr.error('Email already exists or signup failed. Please try again.', 'Signup Failed');
        this.loading = false;
      }
    });
  }
}

