import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserProfile, UpdateSecondaryEmailsRequest } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/user/profile`);
  }

  updateSecondaryEmails(request: UpdateSecondaryEmailsRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/user/profile/secondary-emails`, request);
  }

  updateBasicInfo(request: { name: string, email: string }): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/user/profile/basic`, request);
  }

  sendPortfolioReport(): Observable<string> {
    return this.http.post(`${this.apiUrl}/user/profile/send-report`, {}, { responseType: 'text' });
  }
}

