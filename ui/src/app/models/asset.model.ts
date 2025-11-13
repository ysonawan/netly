export interface Asset {
  id?: number;
  name: string;
  customAssetTypeId: number;
  assetTypeName?: string;
  assetTypeDisplayName?: string;
  currentValue: number;
  purchasePrice?: number;
  purchaseDate?: string;
  quantity?: number;
  description?: string;
  location?: string;
  currency?: string;
  illiquid?: boolean;
  gainLoss?: number;
  gainLossPercentage?: number;
}

export interface PortfolioSummary {
  totalValue: number;
  totalGainLoss: number;
  totalGainLossPercentage: number;
  totalAssets: number;
  breakdown: AssetTypeBreakdown;

  // Liabilities
  totalLiabilities: number;
  totalLiabilityCount: number;
  liabilityBreakdown: LiabilityTypeBreakdown;

  // Net Worth
  netWorth: number;
}

export interface AssetTypeBreakdown {
  typeBreakdown: { [key: string]: number };
}

export interface LiabilityTypeBreakdown {
  typeBreakdown: { [key: string]: number };
}
