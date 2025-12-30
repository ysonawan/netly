import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { LiabilityService } from '../../services/liability.service';
import { ConfigurationService } from '../../services/configuration.service';
import { PortfolioSnapshotService } from '../../services/portfolio-snapshot.service';
import { Asset, PortfolioSummary } from '../../models/asset.model';
import { Liability } from '../../models/liability.model';
import { CustomAssetType } from '../../models/configuration.model';
import { PortfolioHistory, HistoryFilterType } from '../../models/portfolio-snapshot.model';

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

  // Expose enum for template
  HistoryFilterType = HistoryFilterType;

  // Chart data
  chartOptions: any = {};
  liabilityChartOptions: any = {};
  hasChartData = false;
  hasLiabilityChartData = false;

  // Portfolio history
  portfolioHistory: PortfolioHistory | null = null;
  historyChartOptions: any = {};
  hasHistoryData = false;
  selectedHistoryFilter: HistoryFilterType = HistoryFilterType.OVERVIEW;
  selectedHistoryWeeks: number = 52; // Default to 1 year
  historyLoading = false;
  showHistoryFilterDropdown = false;
  selectedAssetForHistory: Asset | null = null;
  selectedLiabilityForHistory: Liability | null = null;
  selectedAssetTypeForHistory: string | null = null;
  selectedLiabilityTypeForHistory: string | null = null;

  // Time period options
  timePeriodOptions = [
    { label: '3 Months', weeks: 13 },
    { label: '6 Months', weeks: 26 },
    { label: '1 Year', weeks: 52 },
    { label: '3 Years', weeks: 156 },
    { label: '5 Years', weeks: 260 },
    { label: '10 Years', weeks: 520 },
    { label: 'All', weeks: 9999 }
  ];

  constructor(
    private assetService: AssetService,
    private liabilityService: LiabilityService,
    private configurationService: ConfigurationService,
    private portfolioSnapshotService: PortfolioSnapshotService,
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
        this.loadPortfolioHistory();
      },
      error: (err) => {
        console.error('Failed to load asset types', err);
        this.loadDashboardData(); // Continue with default behavior if custom types fail to load
        this.loadPortfolioHistory();
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
        currency: currencyCode,
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(value);
    } catch (error) {
      // Fallback if currency code is invalid
      return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
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

  // Portfolio history methods
  loadPortfolioHistory(): void {
    this.historyLoading = true;

    switch (this.selectedHistoryFilter) {
      case HistoryFilterType.OVERVIEW:
        this.portfolioSnapshotService.getPortfolioHistory(this.selectedHistoryWeeks).subscribe({
          next: (history) => {
            this.portfolioHistory = history;
            this.prepareHistoryChart(history);
            this.historyLoading = false;
          },
          error: (error) => {
            console.error('Error loading portfolio history:', error);
            this.historyLoading = false;
            this.hasHistoryData = false;
          }
        });
        break;

      case HistoryFilterType.ASSET:
        if (this.selectedAssetForHistory?.id) {
          this.portfolioSnapshotService.getAssetHistory(this.selectedAssetForHistory.id, this.selectedHistoryWeeks).subscribe({
            next: (history) => {
              this.portfolioHistory = history;
              this.prepareHistoryChart(history);
              this.historyLoading = false;
            },
            error: (error) => {
              console.error('Error loading asset history:', error);
              this.historyLoading = false;
              this.hasHistoryData = false;
            }
          });
        }
        break;

      case HistoryFilterType.ASSET_TYPE:
        if (this.selectedAssetTypeForHistory) {
          this.portfolioSnapshotService.getAssetTypeHistory(this.selectedAssetTypeForHistory, this.selectedHistoryWeeks).subscribe({
            next: (history) => {
              this.portfolioHistory = history;
              this.prepareHistoryChart(history);
              this.historyLoading = false;
            },
            error: (error) => {
              console.error('Error loading asset type history:', error);
              this.historyLoading = false;
              this.hasHistoryData = false;
            }
          });
        }
        break;

      case HistoryFilterType.LIABILITY:
        if (this.selectedLiabilityForHistory?.id) {
          this.portfolioSnapshotService.getLiabilityHistory(this.selectedLiabilityForHistory.id, this.selectedHistoryWeeks).subscribe({
            next: (history) => {
              this.portfolioHistory = history;
              this.prepareHistoryChart(history);
              this.historyLoading = false;
            },
            error: (error) => {
              console.error('Error loading liability history:', error);
              this.historyLoading = false;
              this.hasHistoryData = false;
            }
          });
        }
        break;

      case HistoryFilterType.LIABILITY_TYPE:
        if (this.selectedLiabilityTypeForHistory) {
          this.portfolioSnapshotService.getLiabilityTypeHistory(this.selectedLiabilityTypeForHistory, this.selectedHistoryWeeks).subscribe({
            next: (history) => {
              this.portfolioHistory = history;
              this.prepareHistoryChart(history);
              this.historyLoading = false;
            },
            error: (error) => {
              console.error('Error loading liability type history:', error);
              this.historyLoading = false;
              this.hasHistoryData = false;
            }
          });
        }
        break;
    }
  }

  prepareHistoryChart(history: PortfolioHistory): void {
    if (!history || !history.dates || history.dates.length === 0) {
      this.hasHistoryData = false;
      return;
    }

    this.hasHistoryData = true;
    const isMobile = window.innerWidth < 768;

    const series: any[] = [];

    // Determine which series to show based on filter type
    if (this.selectedHistoryFilter === HistoryFilterType.OVERVIEW) {
      if (history.totalAssets && history.totalAssets.length > 0) {
        series.push({
          name: 'Total Assets',
          type: 'line',
          smooth: true,
          data: history.totalAssets,
          lineStyle: { width: 2.5 },
          itemStyle: { color: '#3B82F6' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
                { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
              ]
            }
          }
        });
      }

      if (history.totalLiabilities && history.totalLiabilities.length > 0) {
        series.push({
          name: 'Total Liabilities',
          type: 'line',
          smooth: true,
          data: history.totalLiabilities,
          lineStyle: { width: 2.5 },
          itemStyle: { color: '#EF4444' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
                { offset: 1, color: 'rgba(239, 68, 68, 0.05)' }
              ]
            }
          }
        });
      }

      if (history.netWorth && history.netWorth.length > 0) {
        series.push({
          name: 'Net Worth',
          type: 'line',
          smooth: true,
          data: history.netWorth,
          lineStyle: { width: 3 },
          itemStyle: { color: '#10B981' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
                { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
              ]
            }
          }
        });
      }

      if (history.totalGains && history.totalGains.length > 0) {
        series.push({
          name: 'Total Gains',
          type: 'line',
          smooth: true,
          data: history.totalGains,
          lineStyle: { width: 2.5 },
          itemStyle: { color: '#F59E0B' }
        });
      }
    } else if (this.selectedHistoryFilter === HistoryFilterType.ASSET ||
               this.selectedHistoryFilter === HistoryFilterType.ASSET_TYPE) {
      if (history.totalAssets && history.totalAssets.length > 0) {
        series.push({
          name: 'Value',
          type: 'line',
          smooth: true,
          data: history.totalAssets,
          lineStyle: { width: 2.5 },
          itemStyle: { color: '#3B82F6' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
                { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
              ]
            }
          }
        });
      }

      if (history.totalGains && history.totalGains.length > 0) {
        series.push({
          name: 'Gains',
          type: 'line',
          smooth: true,
          data: history.totalGains,
          lineStyle: { width: 2.5 },
          itemStyle: { color: '#10B981' }
        });
      }
    } else if (this.selectedHistoryFilter === HistoryFilterType.LIABILITY ||
               this.selectedHistoryFilter === HistoryFilterType.LIABILITY_TYPE) {
      if (history.totalLiabilities && history.totalLiabilities.length > 0) {
        series.push({
          name: 'Balance',
          type: 'line',
          smooth: true,
          data: history.totalLiabilities,
          lineStyle: { width: 2.5 },
          itemStyle: { color: '#EF4444' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
                { offset: 1, color: 'rgba(239, 68, 68, 0.05)' }
              ]
            }
          }
        });
      }
    }

    // Calculate optimal label interval based on data points and screen size
    const dataPointCount = history.dates.length;
    let labelInterval = 0; // 0 means show all labels

    if (isMobile) {
      // Mobile: Show fewer labels based on data point count
      if (dataPointCount > 52) {
        labelInterval = Math.floor(dataPointCount / 8); // Show ~8 labels
      } else if (dataPointCount > 26) {
        labelInterval = Math.floor(dataPointCount / 6); // Show ~6 labels
      } else if (dataPointCount > 12) {
        labelInterval = Math.floor(dataPointCount / 5); // Show ~5 labels
      } else {
        labelInterval = 0; // Show all for small datasets
      }
    } else {
      // Desktop: Show more labels
      if (dataPointCount > 100) {
        labelInterval = Math.floor(dataPointCount / 15); // Show ~15 labels
      } else if (dataPointCount > 52) {
        labelInterval = Math.floor(dataPointCount / 12); // Show ~12 labels
      } else if (dataPointCount > 26) {
        labelInterval = 1; // Show every other label
      } else {
        labelInterval = 0; // Show all
      }
    }

    this.historyChartOptions = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#e5e7eb',
        borderWidth: 1,
        textStyle: { color: '#374151' },
        formatter: (params: any) => {
          let tooltip = `<strong>${params[0].axisValue}</strong><br/>`;
          params.forEach((param: any) => {
            tooltip += `${param.marker} ${param.seriesName}: ${this.formatCurrency(param.value)}<br/>`;
          });
          return tooltip;
        },
        confine: true
      },
      legend: {
        data: series.map(s => s.name),
        bottom: 0,
        textStyle: {
          fontSize: isMobile ? 10 : 12
        },
        itemGap: isMobile ? 8 : 10
      },
      grid: {
        left: '8%',
        right: isMobile ? '3%' : '4%',
        top: '10%',
        bottom: isMobile ? '18%' : '12%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: history.dates,
        axisLabel: {
          fontSize: isMobile ? 9 : 11,
          rotate: isMobile ? 45 : 0,
          interval: labelInterval,
          formatter: (value: string) => {
            const date = new Date(value);
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const year = date.getFullYear();

            if (isMobile) {
              // For mobile, show month/day or just month/year for large datasets
              if (dataPointCount > 52) {
                return `${month}/${year.toString().substr(2)}`;
              } else {
                return `${month}/${day}`;
              }
            } else {
              // For desktop, show full date
              return `${month}/${day}/${year.toString().substr(2)}`;
            }
          },
          hideOverlap: true
        },
        axisTick: {
          alignWithLabel: true,
          interval: labelInterval
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          fontSize: isMobile ? 9 : 11,
          formatter: (value: number) => {
            if (value >= 10000000) return `${(value / 10000000).toFixed(1)}Cr`;
            if (value >= 100000) return `${(value / 100000).toFixed(1)}L`;
            if (value >= 1000) return `${(value / 1000).toFixed(1)}K`;
            return value.toString();
          }
        },
        splitLine: {
          lineStyle: {
            type: 'dashed',
            opacity: 0.3
          }
        }
      },
      series: series
    };
  }

  onHistoryFilterChange(filterType: HistoryFilterType, id?: number, name?: string): void {
    this.selectedHistoryFilter = filterType;
    this.showHistoryFilterDropdown = false;

    switch (filterType) {
      case HistoryFilterType.ASSET:
        this.selectedAssetForHistory = this.assets.find(a => a.id === id) || null;
        break;
      case HistoryFilterType.LIABILITY:
        this.selectedLiabilityForHistory = this.liabilities.find(l => l.id === id) || null;
        break;
      case HistoryFilterType.ASSET_TYPE:
        this.selectedAssetTypeForHistory = name || null;
        break;
      case HistoryFilterType.LIABILITY_TYPE:
        this.selectedLiabilityTypeForHistory = name || null;
        break;
      default:
        this.selectedAssetForHistory = null;
        this.selectedLiabilityForHistory = null;
        this.selectedAssetTypeForHistory = null;
        this.selectedLiabilityTypeForHistory = null;
    }

    this.loadPortfolioHistory();
  }

  onHistoryWeeksChange(weeks: number): void {
    this.selectedHistoryWeeks = weeks;
    this.loadPortfolioHistory();
  }

  getHistoryFilterLabel(): string {
    switch (this.selectedHistoryFilter) {
      case HistoryFilterType.OVERVIEW:
        return 'Portfolio Overview';
      case HistoryFilterType.ASSET:
        return this.selectedAssetForHistory?.name || 'Select Asset';
      case HistoryFilterType.ASSET_TYPE:
        return this.selectedAssetTypeForHistory || 'Select Asset Type';
      case HistoryFilterType.LIABILITY:
        return this.selectedLiabilityForHistory?.name || 'Select Liability';
      case HistoryFilterType.LIABILITY_TYPE:
        return this.selectedLiabilityTypeForHistory || 'Select Liability Type';
      default:
        return 'Portfolio Overview';
    }
  }

  getUniqueAssetTypes(): string[] {
    const types = new Set<string>();
    this.assets.forEach(asset => {
      if (asset.assetTypeDisplayName) {
        types.add(asset.assetTypeDisplayName);
      }
    });
    return Array.from(types).sort();
  }

  getUniqueLiabilityTypes(): string[] {
    const types = new Set<string>();
    this.liabilities.forEach(liability => {
      if (liability.liabilityTypeDisplayName) {
        types.add(liability.liabilityTypeDisplayName);
      }
    });
    return Array.from(types).sort();
  }
}

