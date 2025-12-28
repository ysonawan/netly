export interface PortfolioSnapshot {
  id: number;
  snapshotDate: string;
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  totalGains: number;
  assetSnapshots?: AssetSnapshot[];
  liabilitySnapshots?: LiabilitySnapshot[];
}

export interface AssetSnapshot {
  id: number;
  assetId: number;
  assetName: string;
  assetTypeName: string;
  currentValue: number;
  gainLoss: number;
  currency: string;
  valueInInr: number;
}

export interface LiabilitySnapshot {
  id: number;
  liabilityId: number;
  liabilityName: string;
  liabilityTypeName: string;
  currentBalance: number;
  currency: string;
  balanceInInr: number;
}

export interface PortfolioHistory {
  dates: string[];
  totalAssets: number[];
  totalLiabilities: number[];
  netWorth: number[];
  totalGains: number[];
}

export enum HistoryFilterType {
  OVERVIEW = 'overview',
  ASSET = 'asset',
  ASSET_TYPE = 'asset_type',
  LIABILITY = 'liability',
  LIABILITY_TYPE = 'liability_type'
}

export interface HistoryFilter {
  type: HistoryFilterType;
  id?: number;
  name?: string;
  weeks?: number;
}

