import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { LiabilityService } from '../../services/liability.service';
import { ConfigurationService } from '../../services/configuration.service';
import { Asset, PortfolioSummary } from '../../models/asset.model';
import { Liability } from '../../models/liability.model';
import { CustomAssetType } from '../../models/configuration.model';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],
    standalone: false
})
export class DashboardComponent implements OnInit {
  summary: PortfolioSummary | null = null;
  assets: Asset[] = [];
  liabilities: Liability[] = [];
  loading = true;
  userCurrency: string = 'INR'; // Default currency, can be set from user preferences
  customAssetTypes: CustomAssetType[] = [];

  // Chart data
  chartData: any;
  chartOptions: any;
  liabilityChartData: any;
  liabilityChartOptions: any;

  constructor(
    private assetService: AssetService,
    private liabilityService: LiabilityService,
    private configurationService: ConfigurationService,
    private router: Router
  ) {
    this.initializeChartOptions();
    this.initializeLiabilityChartOptions();
    this.loadUserCurrency();
  }

  ngOnInit(): void {
    this.loadCustomAssetTypes();
  }

  loadUserCurrency(): void {
    // Try to load user's preferred currency from localStorage or user settings
    const savedCurrency = localStorage.getItem('userCurrency');
    if (savedCurrency) {
      this.userCurrency = savedCurrency;
    }
  }

  loadCustomAssetTypes(): void {
    this.configurationService.getAllCustomAssetTypes().subscribe({
      next: (types) => {
        this.customAssetTypes = types.filter(t => t.isActive !== false);
        this.loadDashboardData();
      },
      error: (err) => {
        console.error('Failed to load asset types', err);
        this.loadDashboardData(); // Continue with default behavior if custom types fail to load
      }
    });
  }

  loadDashboardData(): void {
    this.loading = true;

    this.assetService.getPortfolioSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.prepareChartData(summary);
        this.prepareLiabilityChartDataInternal(summary);
      },
      error: (error) => {
        console.error('Error loading summary:', error);
        this.loading = false;
      }
    });

    this.assetService.getAllAssets().subscribe({
      next: (assets) => {
        this.assets = assets;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading assets:', error);
        this.loading = false;
      }
    });

    this.liabilityService.getAllLiabilities().subscribe({
      next: (liabilities) => {
        this.liabilities = liabilities;
      },
      error: (error) => {
        console.error('Error loading liabilities:', error);
      }
    });
  }

  prepareChartData(summary: PortfolioSummary): void {
    const breakdown = summary.breakdown;

    // Create labels and data arrays from the dynamic typeBreakdown Map
    const labels: string[] = [];
    const data: number[] = [];
    const colors: string[] = [];

    // Use the dynamic typeBreakdown Map and sort by value descending (same as getAssetBreakdownEntries)
    if (breakdown.typeBreakdown) {
      const typeBreakdownEntries = Object.entries(breakdown.typeBreakdown)
        .map(([name, value]) => ({name, value}))
        .filter(entry => entry.value > 0)
        .sort((a, b) => b.value - a.value); // Sort by value descending to match Asset Breakdown

      typeBreakdownEntries.forEach((entry, index) => {
        labels.push(entry.name);
        data.push(entry.value);
        colors.push(this.getColorForIndex(index));
      });
    }

    this.chartData = {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: colors
      }]
    };
  }

  initializeChartOptions(): void {
    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'right',
          labels: {
            boxWidth: 12,
            font: {
              size: 11
            },
            padding: 8
          }
        }
      }
    };
  }

  initializeLiabilityChartOptions(): void {
    this.liabilityChartOptions = {
      responsive: true,
      maintainAspectRatio: true,
      cutout: '65%', // Makes it a donut chart
      plugins: {
        legend: {
          position: 'right',
          labels: {
            boxWidth: 12,
            font: {
              size: 11
            },
            padding: 8
          }
        }
      }
    };
  }

  formatCurrency(value: number | undefined, currency?: string): string {
    if (!value && value !== 0) return this.formatZero(currency);
    const currencyCode = currency || this.userCurrency || 'INR';
    try {
      return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: currencyCode
      }).format(value);
    } catch (error) {
      // Fallback if currency code is invalid
      return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR'
      }).format(value);
    }
  }

  formatZero(currency?: string): string {
    const currencyCode = currency || this.userCurrency || 'INR';
    try {
      return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: currencyCode
      }).format(0);
    } catch (error) {
      return '₹0.00';
    }
  }

  formatPercentage(value: number | undefined): string {
    if (!value) return '0.00%';
    return `${value.toFixed(2)}%`;
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString();
  }

  deleteLiability(liability: Liability): void {
    if (confirm(`Are you sure you want to delete ${liability.name}?`)) {
      this.liabilityService.deleteLiability(liability.id!).subscribe({
        next: () => {
          this.loadDashboardData();
        },
        error: (error) => {
          console.error('Error deleting liability:', error);
          alert('Failed to delete liability');
        }
      });
    }
  }

  // Helper method to get breakdown entries for display in templates
  getAssetBreakdownEntries(): Array<{name: string, value: number}> {
    if (!this.summary?.breakdown?.typeBreakdown) {
      return [];
    }
    return Object.entries(this.summary.breakdown.typeBreakdown)
      .map(([name, value]) => ({name, value}))
      .filter(entry => entry.value > 0)
      .sort((a, b) => b.value - a.value); // Sort by value descending
  }

  // Get color for index to match chart colors
  getColorForIndex(index: number): string {
    const colors = [
      '#FF8C00', // Dark Orange
      '#0099CC', // Ocean Blue
      '#99CC00', // Olive Green
      '#9900CC', // Deep Purple
      '#00CC99', // Teal
      '#FF00FF', // Pure Magenta
      '#808080', // Gray
      '#00FF00', // Pure Green
      '#FF0000', // Pure Red
      '#00FFFF', // Pure Cyan
      '#FF6600', // Orange
      '#9900FF', // Purple
      '#0000FF', // Pure Blue
      '#00FF99', // Mint
      '#CC00FF',  // Electric Purple
      '#CC9900', // Gold
      '#FF0099', // Hot Pink
      '#99FF00', // Lime
      '#0099FF', // Sky Blue
      '#CC0099' // Violet
    ];
    return colors[index % colors.length];
  }

  // Get top performing assets
  getTopAssets(count: number = 5): Asset[] {
    return [...this.assets]
      .filter(asset => asset.gainLossPercentage !== undefined && asset.gainLossPercentage !== null)
      .sort((a, b) => {
        const gainA = a.gainLossPercentage || 0;
        const gainB = b.gainLossPercentage || 0;
        return gainB - gainA;
      })
      .slice(0, count);
  }

  // Calculate allocation percentage for visualization
  calculateAllocationPercentage(value: number, total: number): number {
    if (total === 0) return 0;
    return (value / total) * 100;
  }

  // Get asset allocation with colors for progress bars
  getAssetAllocationWithColors(): Array<{name: string, value: number, percentage: number, color: string}> {
    const entries = this.getAssetBreakdownEntries();
    const total = this.summary?.totalValue || 0;

    return entries.map((entry, index) => ({
      name: entry.name,
      value: entry.value,
      percentage: this.calculateAllocationPercentage(entry.value, total),
      color: this.getColorForIndex(index)
    }));
  }

  // Helper method to get liability breakdown entries for display
  getLiabilityBreakdownEntries(): Array<{name: string, value: number}> {
    if (!this.summary?.liabilityBreakdown?.typeBreakdown) {
      return [];
    }
    return Object.entries(this.summary.liabilityBreakdown.typeBreakdown)
      .map(([name, value]) => ({name, value}))
      .filter(entry => entry.value > 0)
      .sort((a, b) => b.value - a.value); // Sort by value descending
  }

  // Method to prepare liability chart data (if needed for separate liability chart)
  prepareLiabilityChartDataInternal(summary: PortfolioSummary): void {
    const liabilityBreakdown = summary?.liabilityBreakdown;
    if (!liabilityBreakdown?.typeBreakdown) {
      this.liabilityChartData = null;
      return;
    }

    const labels: string[] = [];
    const data: number[] = [];
    const colors = ['#EF4444', '#F97316', '#F59E0B', '#EAB308', '#84CC16', '#22C55E', '#10B981'];

    Object.entries(liabilityBreakdown.typeBreakdown).forEach(([typeName, value]) => {
      if (value > 0) {
        labels.push(typeName);
        data.push(value);
      }
    });

    if (data.length > 0) {
      this.liabilityChartData = {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: colors.slice(0, labels.length)
        }]
      };
    } else {
      this.liabilityChartData = null;
    }
  }

  // Navigation methods
  navigateToAssets(): void {
    this.router.navigate(['/assets']);
  }

  navigateToLiabilities(): void {
    this.router.navigate(['/liabilities']);
  }

  navigateToAddAsset(): void {
    this.router.navigate(['/assets/new']);
  }

  navigateToAddLiability(): void {
    this.router.navigate(['/liabilities/new']);
  }
}
