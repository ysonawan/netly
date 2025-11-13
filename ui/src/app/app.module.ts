import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideCharts, withDefaultRegisterables, BaseChartDirective } from 'ng2-charts';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AssetListComponent } from './components/asset-list/asset-list.component';
import { AssetFormComponent } from './components/asset-form/asset-form.component';
import { LiabilityListComponent } from './components/liability-list/liability-list.component';
import { LiabilityFormComponent } from './components/liability-form/liability-form.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { SettingsComponent } from './components/settings/settings.component';
import { AssetService } from './services/asset.service';
import { LiabilityService } from './services/liability.service';
import { AuthService } from './services/auth.service';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import {ProfileComponent} from "./components/profile/profile.component";

@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        AssetListComponent,
        AssetFormComponent,
        LiabilityListComponent,
        LiabilityFormComponent,
        LoginComponent,
        SignupComponent,
        SettingsComponent,
        ProfileComponent
    ],
    bootstrap: [AppComponent],
    imports: [
        BrowserModule,
        CommonModule,
        AppRoutingModule,
        FormsModule,
        ReactiveFormsModule,
        BaseChartDirective
    ],
    providers: [
        AssetService,
        LiabilityService,
        AuthService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideCharts(withDefaultRegisterables())
    ]
})
export class AppModule { }

