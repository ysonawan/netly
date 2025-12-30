import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CurrencyRate, CustomAssetType, CustomLiabilityType } from '../models/configuration.model';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Currency Rate Configuration
  getAllCurrencyRates(): Observable<CurrencyRate[]> {
    return this.http.get<CurrencyRate[]>(`${this.apiUrl}/configuration/currency-rates`);
  }

  saveCurrencyRate(rate: CurrencyRate): Observable<CurrencyRate> {
    return this.http.post<CurrencyRate>(`${this.apiUrl}/configuration/currency-rates`, rate);
  }

  deleteCurrencyRate(currencyCode: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/configuration/currency-rates/${currencyCode}`);
  }

  // Custom Asset Types
  getAllCustomAssetTypes(): Observable<CustomAssetType[]> {
    return this.http.get<CustomAssetType[]>(`${this.apiUrl}/configuration/custom-asset-types`);
  }

  saveCustomAssetType(customType: CustomAssetType): Observable<CustomAssetType> {
    return this.http.post<CustomAssetType>(`${this.apiUrl}/configuration/custom-asset-types`, customType);
  }

  deleteCustomAssetType(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/configuration/custom-asset-types/${id}`);
  }

  // Custom Liability Types
  getAllCustomLiabilityTypes(): Observable<CustomLiabilityType[]> {
    return this.http.get<CustomLiabilityType[]>(`${this.apiUrl}/configuration/custom-liability-types`);
  }

  saveCustomLiabilityType(customType: CustomLiabilityType): Observable<CustomLiabilityType> {
    return this.http.post<CustomLiabilityType>(`${this.apiUrl}/configuration/custom-liability-types`, customType);
  }

  deleteCustomLiabilityType(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/configuration/custom-liability-types/${id}`);
  }
}
