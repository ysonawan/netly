export interface CurrencyRate {
  id?: number;
  currencyCode: string;
  currencyName: string;
  rateToInr: number;
  isActive: boolean;
}

export interface CustomAssetType {
  id?: number;
  typeName?: string;
  displayName: string;
  description?: string;
  isActive: boolean;
}

export interface CustomLiabilityType {
  id?: number;
  typeName?: string;
  displayName: string;
  description?: string;
  isActive: boolean;
}
