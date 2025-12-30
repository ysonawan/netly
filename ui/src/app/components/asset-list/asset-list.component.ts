import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { Asset } from '../../models/asset.model';
import Swal from 'sweetalert2';
import {CustomAssetType, CurrencyRate} from "../../models/configuration.model";
import {ConfigurationService} from "../../services/configuration.service";

@Component({
    selector: 'app-asset-list',
    templateUrl: './asset-list.component.html',
    styleUrls: ['./asset-list.component.css'],
    standalone: false
})
export class AssetListComponent implements OnInit {
  assets: Asset[] = [];
  filteredAssets: Asset[] = [];
  loading = true;
  filterType: string = 'ALL';
  filterLiquidity: string = 'ALL';
  searchTerm: string = '';
  userCurrency: string = 'INR';

  // Summary statistics
  totalPurchasePrice: number = 0;
  totalCurrentValue: number = 0;
  totalGains: number = 0;
  totalGainsPercentage: number = 0;

  assetTypeOptions: CustomAssetType[] = [];
  currencyRates: CurrencyRate[] = [];
  currencyRateMap: Map<string, number> = new Map();

  constructor(
    private assetService: AssetService,
    private router: Router,
    private configurationService: ConfigurationService
  ) {
    this.loadUserCurrency();
  }

  ngOnInit(): void {
    this.configurationService.getAllCustomAssetTypes().subscribe({
      next: (types) => {
        this.assetTypeOptions = types;
      },
      error: (err) => {
        console.error('Failed to load asset types', err);
      }
    });
    this.loadCurrencyRates();
    this.loadAssets();
  }

  loadUserCurrency(): void {
    const savedCurrency = localStorage.getItem('userCurrency');
    if (savedCurrency) {
      this.userCurrency = savedCurrency;
    }
  }

  loadCurrencyRates(): void {
    this.configurationService.getAllCurrencyRates().subscribe({
      next: (rates) => {
        this.currencyRates = rates;
        // Create a map for quick lookup
        this.currencyRateMap.clear();
        rates.forEach(rate => {
          if (rate.isActive) {
            this.currencyRateMap.set(rate.currencyCode, rate.rateToInr);
          }
        });
        // INR to INR rate is always 1
        this.currencyRateMap.set('INR', 1);
      },
      error: (err) => {
        console.error('Failed to load currency rates', err);
        // Set default INR rate
        this.currencyRateMap.set('INR', 1);
      }
    });
  }

  loadAssets(): void {
    this.loading = true;
    this.assetService.getAllAssets().subscribe({
      next: (assets) => {
        this.assets = assets;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading assets:', error);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredAssets = this.assets.filter(asset => {
      const matchesType = this.filterType === 'ALL' || asset.assetTypeDisplayName === this.filterType;
      const matchesSearch = asset.name.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesLiquidity = this.filterLiquidity === 'ALL' ||
          (this.filterLiquidity === 'ILLIQUID' && asset.illiquid) ||
          (this.filterLiquidity === 'LIQUID' && !asset.illiquid);
      return matchesType && matchesSearch && matchesLiquidity;
    });
    this.calculateSummaryStatistics();
  }

  calculateSummaryStatistics(): void {
    this.totalPurchasePrice = 0;
    this.totalCurrentValue = 0;
    this.totalGains = 0;
    this.totalGainsPercentage = 0;

    this.filteredAssets.forEach(asset => {
      const currency = asset.currency || 'INR';
      const rate = this.currencyRateMap.get(currency) || 1;
      const quantity = asset.quantity || 1;

      // Convert to INR and multiply purchase price by quantity
      const purchasePriceInINR = (asset.purchasePrice || 0) * quantity * rate;
      const currentValueInINR = (asset.currentValue || 0) * rate;

      this.totalPurchasePrice += purchasePriceInINR;
      this.totalCurrentValue += currentValueInINR;
    });

    this.totalGains = this.totalCurrentValue - this.totalPurchasePrice;
    this.totalGainsPercentage = this.totalPurchasePrice > 0 ? (this.totalGains / this.totalPurchasePrice) * 100 : 0;
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  editAsset(asset: Asset): void {
    this.router.navigate(['/assets/edit', asset.id]);
  }

  deleteAsset(asset: Asset): void {
    Swal.fire({
      title: 'Are you sure?',
      text: `Do you want to delete ${asset.name}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.assetService.deleteAsset(asset.id!).subscribe({
          next: () => {
            Swal.fire('Deleted!', 'Asset has been deleted.', 'success');
            this.loadAssets();
          },
          error: (error) => {
            console.error('Error deleting asset:', error);
            Swal.fire('Error!', 'Failed to delete asset', 'error');
          }
        });
      }
    });
  }

  addNewAsset(): void {
    this.router.navigate(['/assets/new']);
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

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString();
  }
}
