import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PortfolioSnapshot, PortfolioHistory } from '../models/portfolio-snapshot.model';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class PortfolioSnapshotService {
  private apiUrl = `${environment.apiUrl}/portfolio-snapshots`;

  constructor(private http: HttpClient) { }

  createSnapshot(): Observable<PortfolioSnapshot> {
    return this.http.post<PortfolioSnapshot>(this.apiUrl, {});
  }

  getAllSnapshots(): Observable<PortfolioSnapshot[]> {
    return this.http.get<PortfolioSnapshot[]>(this.apiUrl);
  }

  getSnapshotsByDateRange(startDate: string, endDate: string): Observable<PortfolioSnapshot[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<PortfolioSnapshot[]>(`${this.apiUrl}/range`, { params });
  }

  getPortfolioHistory(weeks: number = 12): Observable<PortfolioHistory> {
    const params = new HttpParams().set('weeks', weeks.toString());
    return this.http.get<PortfolioHistory>(`${this.apiUrl}/history`, { params });
  }

  getAssetHistory(assetId: number, weeks: number = 12): Observable<PortfolioHistory> {
    const params = new HttpParams().set('weeks', weeks.toString());
    return this.http.get<PortfolioHistory>(`${this.apiUrl}/history/asset/${assetId}`, { params });
  }

  getAssetTypeHistory(assetTypeName: string, weeks: number = 12): Observable<PortfolioHistory> {
    const params = new HttpParams().set('weeks', weeks.toString());
    return this.http.get<PortfolioHistory>(`${this.apiUrl}/history/asset-type/${encodeURIComponent(assetTypeName)}`, { params });
  }

  getLiabilityHistory(liabilityId: number, weeks: number = 12): Observable<PortfolioHistory> {
    const params = new HttpParams().set('weeks', weeks.toString());
    return this.http.get<PortfolioHistory>(`${this.apiUrl}/history/liability/${liabilityId}`, { params });
  }

  getLiabilityTypeHistory(liabilityTypeName: string, weeks: number = 12): Observable<PortfolioHistory> {
    const params = new HttpParams().set('weeks', weeks.toString());
    return this.http.get<PortfolioHistory>(`${this.apiUrl}/history/liability-type/${encodeURIComponent(liabilityTypeName)}`, { params });
  }
}

