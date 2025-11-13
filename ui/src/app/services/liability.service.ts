import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Liability } from '../models/liability.model';

@Injectable({
  providedIn: 'root'
})
export class LiabilityService {
  private apiUrl = '/api/liabilities';

  constructor(private http: HttpClient) {}

  getAllLiabilities(): Observable<Liability[]> {
    return this.http.get<Liability[]>(this.apiUrl);
  }

  getLiabilityById(id: number): Observable<Liability> {
    return this.http.get<Liability>(`${this.apiUrl}/${id}`);
  }

  getLiabilitiesByType(type: string): Observable<Liability[]> {
    return this.http.get<Liability[]>(`${this.apiUrl}/type/${type}`);
  }

  createLiability(liability: Liability): Observable<Liability> {
    return this.http.post<Liability>(this.apiUrl, liability);
  }

  updateLiability(id: number, liability: Liability): Observable<Liability> {
    return this.http.put<Liability>(`${this.apiUrl}/${id}`, liability);
  }

  deleteLiability(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

