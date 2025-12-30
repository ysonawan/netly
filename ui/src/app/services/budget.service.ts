import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { BudgetItem, BudgetSummary } from '../models/budget.model';

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private apiUrl = `${environment.apiUrl}/budget`;

  constructor(private http: HttpClient) {}

  getAllBudgetItems(): Observable<BudgetItem[]> {
    return this.http.get<BudgetItem[]>(this.apiUrl);
  }

  getBudgetItemsByType(type: string): Observable<BudgetItem[]> {
    return this.http.get<BudgetItem[]>(`${this.apiUrl}/type/${type}`);
  }

  getBudgetItemById(id: number): Observable<BudgetItem> {
    return this.http.get<BudgetItem>(`${this.apiUrl}/${id}`);
  }

  createBudgetItem(budgetItem: BudgetItem): Observable<BudgetItem> {
    return this.http.post<BudgetItem>(this.apiUrl, budgetItem);
  }

  updateBudgetItem(id: number, budgetItem: BudgetItem): Observable<BudgetItem> {
    return this.http.put<BudgetItem>(`${this.apiUrl}/${id}`, budgetItem);
  }

  deleteBudgetItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getBudgetSummary(): Observable<BudgetSummary> {
    return this.http.get<BudgetSummary>(`${this.apiUrl}/summary`);
  }
}

