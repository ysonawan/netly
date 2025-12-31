import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { Asset } from '../../models/asset.model';
import Swal from 'sweetalert2';
import {CustomAssetType} from "../../models/configuration.model";
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

  // Summary statistics
  totalPurchasePrice: number = 0;
  totalCurrentValue: number = 0;
  totalGains: number = 0;
  totalGainsPercentage: number = 0;

  assetTypeOptions: CustomAssetType[] = [];

  constructor(
    private assetService: AssetService,
    private router: Router,
    private configurationService: ConfigurationService
  ) {}

  ngOnInit(): void {
    this.configurationService.getAllCustomAssetTypes().subscribe({
      next: (types) => {
        this.assetTypeOptions = types;
      },
      error: (err) => {
        console.error('Failed to load asset types', err);
      }
    });
    this.loadAssets();
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
      const quantity = asset.quantity || 1;

      // All amounts are in INR, multiply purchase price by quantity
      this.totalPurchasePrice += (asset.purchasePrice || 0) * quantity;
      this.totalCurrentValue += (asset.currentValue || 0);
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

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString();
  }
}

