export interface BudgetItem {
  id?: number;
  itemType: 'INCOME' | 'EXPENSE';
  itemName: string;
  amount: number;
  isInvestment: boolean;
  description?: string;
  displayOrder?: number;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface BudgetSummary {
  totalIncome: number;
  totalExpenses: number;
  totalInvestments: number;
  totalNonInvestmentExpenses: number;
  totalSurplus: number;
  investmentPercentage: number;
  nonInvestmentExpensePercentage: number;
  savingsRate: number;
}

