export interface Liability {
  id?: number;
  name: string;
  customLiabilityTypeId: number;
  liabilityTypeName?: string;
  liabilityTypeDisplayName?: string;
  currentBalance: number;
  originalAmount?: number;
  startDate?: string;
  endDate?: string;
  interestRate?: number;
  monthlyPayment?: number;
  lender?: string;
  description?: string;
  currency?: string;
  paidAmount?: number;
  repaymentPercentage?: number;
}
