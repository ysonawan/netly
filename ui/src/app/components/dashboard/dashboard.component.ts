import { Component, OnInit, HostListener } from '@angular/core';
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
  chartOptions: any = {};
  liabilityChartOptions: any = {};
  hasChartData = false;
  hasLiabilityChartData = false;

  constructor(
    private assetService: AssetService,
    private liabilityService: LiabilityService,
    private configurationService: ConfigurationService,
    private router: Router
  ) {
    this.loadUserCurrency();
  }

  ngOnInit(): void {
    this.loadCustomAssetTypes();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event?: any): void {
    // Refresh chart options on resize to adapt to new viewport size
    if (this.summary) {
      this.prepareChartData(this.summary);
      this.prepareLiabilityChartDataInternal(this.summary);
    }
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

    // Create data array from the dynamic typeBreakdown Map
    const data: Array<{name: string, value: number}> = [];

    // Use the dynamic typeBreakdown Map and sort by value descending
    if (breakdown.typeBreakdown) {
      const typeBreakdownEntries = Object.entries(breakdown.typeBreakdown)
        .map(([name, value]) => ({name, value}))
        .filter(entry => entry.value > 0)
        .sort((a, b) => b.value - a.value); // Sort by value descending to match Asset Breakdown

      typeBreakdownEntries.forEach((entry, index) => {
        data.push({
          name: entry.name,
          value: entry.value,
          itemStyle: { color: this.getColorForIndex(index) }
        } as any);
      });
    }

    this.hasChartData = data.length > 0;

    // Check if mobile viewport
    const isMobile = window.innerWidth < 640;

    this.chartOptions = {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          const formattedValue = this.formatCurrency(params.value);
          return `${params.seriesName}<br/>${params.name}: ${formattedValue} (${params.percent}%)`;
        },
        confine: true
      },
      legend: {
        orient: isMobile ? 'horizontal' : 'vertical',
        bottom: isMobile ? 0 : undefined,
        right: isMobile ? undefined : 10,
        top: isMobile ? undefined : 'center',
        left: isMobile ? 'center' : undefined,
        textStyle: {
          fontSize: isMobile ? 10 : 11
        },
        itemWidth: 12,
        itemHeight: 12,
        itemGap: isMobile ? 8 : 10,
        padding: isMobile ? [0, 5] : [5, 5],
        tooltip: {
          show: true,
          formatter: (params: any) => {
            // For legend tooltip, ECharts passes the data object directly
            const dataItem = data.find((item: any) => item.name === params.name);
            if (dataItem) {
              // Calculate percentage
              const total = data.reduce((sum: number, item: any) => sum + item.value, 0);
              const percent = ((dataItem.value / total) * 100).toFixed(1);
              const formattedValue = this.formatCurrency(dataItem.value);
              return `Assets<br/>${params.name}: ${formattedValue} (${percent}%)`;
            }
            return params.name;
          }
        }
      },
      series: [
        {
          name: 'Assets',
          type: 'pie',
          radius: isMobile ? '50%' : '65%',
          center: isMobile ? ['50%', '42%'] : ['40%', '50%'],
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            show: false
          }
        }
      ]
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
      this.liabilityChartOptions = {};
      this.hasLiabilityChartData = false;
      return;
    }

    const data: Array<{name: string, value: number}> = [];
    const colors = ['#EF4444', '#F97316', '#F59E0B', '#EAB308', '#84CC16', '#22C55E', '#10B981'];
    let colorIndex = 0;

    Object.entries(liabilityBreakdown.typeBreakdown).forEach(([typeName, value]) => {
      if (value > 0) {
        data.push({
          name: typeName,
          value: value,
          itemStyle: { color: colors[colorIndex % colors.length] }
        } as any);
        colorIndex++;
      }
    });

    this.hasLiabilityChartData = data.length > 0;

    if (data.length > 0) {
      // Check if mobile viewport
      const isMobile = window.innerWidth < 640;

      this.liabilityChartOptions = {
        tooltip: {
          trigger: 'item',
          formatter: (params: any) => {
            const formattedValue = this.formatCurrency(params.value);
            return `${params.seriesName}<br/>${params.name}: ${formattedValue} (${params.percent}%)`;
          },
          confine: true
        },
        legend: {
          orient: isMobile ? 'horizontal' : 'vertical',
          bottom: isMobile ? 0 : undefined,
          right: isMobile ? undefined : 10,
          top: isMobile ? undefined : 'center',
          left: isMobile ? 'center' : undefined,
          textStyle: {
            fontSize: isMobile ? 10 : 11
          },
          itemWidth: 12,
          itemHeight: 12,
          itemGap: isMobile ? 8 : 10,
          padding: isMobile ? [0, 5] : [5, 5],
          tooltip: {
            show: true,
            formatter: (params: any) => {
              // For legend tooltip, ECharts passes the data object directly
              const dataItem = data.find((item: any) => item.name === params.name);
              if (dataItem) {
                // Calculate percentage
                const total = data.reduce((sum: number, item: any) => sum + item.value, 0);
                const percent = ((dataItem.value / total) * 100).toFixed(1);
                const formattedValue = this.formatCurrency(dataItem.value);
                return `Liabilities<br/>${params.name}: ${formattedValue} (${percent}%)`;
              }
              return params.name;
            }
          }
        },
        series: [
          {
            name: 'Liabilities',
            type: 'pie',
            radius: isMobile ? ['30%', '50%'] : ['40%', '65%'], // Donut chart
            center: isMobile ? ['50%', '42%'] : ['40%', '50%'],
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              show: false
            }
          }
        ]
      };
    } else {
      this.liabilityChartOptions = {};
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
