import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AssetListComponent } from './components/asset-list/asset-list.component';
import { AssetFormComponent } from './components/asset-form/asset-form.component';
import { LiabilityListComponent } from './components/liability-list/liability-list.component';
import { LiabilityFormComponent } from './components/liability-form/liability-form.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { AuthGuard } from './guards/auth.guard';
import {SettingsComponent} from "./components/settings/settings.component";

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'assets', component: AssetListComponent, canActivate: [AuthGuard] },
  { path: 'assets/new', component: AssetFormComponent, canActivate: [AuthGuard] },
  { path: 'assets/edit/:id', component: AssetFormComponent, canActivate: [AuthGuard] },
  { path: 'liabilities', component: LiabilityListComponent, canActivate: [AuthGuard] },
  { path: 'liabilities/new', component: LiabilityFormComponent, canActivate: [AuthGuard] },
  { path: 'liabilities/edit/:id', component: LiabilityFormComponent, canActivate: [AuthGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

