import { Component, OnInit } from '@angular/core';
import { ConfigurationService } from '../../services/configuration.service';
import { CurrencyRate, CustomAssetType, CustomLiabilityType } from '../../models/configuration.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-settings',
  standalone: false,
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  activeTab: 'asset-types' | 'liability-types' | 'currencies' = 'asset-types';

  currencyRates: CurrencyRate[] = [];
  editingCurrency: string | null = null;
  newCurrency: CurrencyRate = this.getEmptyCurrency();
  showAddCurrency: boolean = false;

  // Custom asset and liability types
  customAssetTypes: CustomAssetType[] = [];
  newCustomAssetType: CustomAssetType = this.getEmptyCustomAssetType();
  editingCustomAssetType: string | null = null;
  showAddCustomAssetType: boolean = false;

  customLiabilityTypes: CustomLiabilityType[] = [];
  newCustomLiabilityType: CustomLiabilityType = this.getEmptyCustomLiabilityType();
  editingCustomLiabilityType: string | null = null;
  showAddCustomLiabilityType: boolean = false;

  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private configService: ConfigurationService) {}

  ngOnInit(): void {
    this.loadAllConfigurations();
  }

  loadAllConfigurations(): void {
    this.loadCurrencyRates();
    this.loadCustomAssetTypes();
    this.loadCustomLiabilityTypes();
  }

  loadCurrencyRates(): void {
    this.loading = true;
    this.configService.getAllCurrencyRates().subscribe({
      next: (rates) => {
        this.currencyRates = rates;
        this.loading = false;
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to load currency rates');
        this.loading = false;
      }
    });
  }

  saveCurrencyRate(rate: CurrencyRate): void {
    this.configService.saveCurrencyRate(rate).subscribe({
      next: () => {
        this.showSuccess('Currency rate saved successfully');
        this.editingCurrency = null;
        this.loadCurrencyRates();
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to save currency rate');
      }
    });
  }

  addNewCurrency(): void {
    if (!this.newCurrency.currencyCode || !this.newCurrency.currencyName || !this.newCurrency.rateToInr) {
      this.showError('Please fill in all fields');
      return;
    }

    this.configService.saveCurrencyRate(this.newCurrency).subscribe({
      next: () => {
        this.showSuccess('Currency added successfully');
        this.newCurrency = this.getEmptyCurrency();
        this.showAddCurrency = false;
        this.loadCurrencyRates();
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to add currency');
      }
    });
  }

  toggleAddCurrency(): void {
    this.showAddCurrency = !this.showAddCurrency;
    if (!this.showAddCurrency) {
      this.newCurrency = this.getEmptyCurrency();
    }
  }

  deleteCurrencyRate(currencyCode: string): void {
    if (currencyCode === 'INR') {
      this.showError('Cannot delete INR currency');
      return;
    }

    Swal.fire({
      title: 'Are you sure?',
      text: `Do you want to delete ${currencyCode} currency?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.configService.deleteCurrencyRate(currencyCode).subscribe({
          next: () => {
            Swal.fire('Deleted!', 'Currency has been deleted.', 'success');
            this.loadCurrencyRates();
          },
          error: (err) => {
            Swal.fire('Error!', err?.error?.message || 'Failed to delete currency', 'error');
          }
        });
      }
    });
  }

  // Custom Asset Type Methods
  loadCustomAssetTypes(): void {
    this.configService.getAllCustomAssetTypes().subscribe({
      next: (types) => {
        this.customAssetTypes = types;
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to load custom asset types');
      }
    });
  }

  toggleAddCustomAssetType(): void {
    this.showAddCustomAssetType = !this.showAddCustomAssetType;
    if (!this.showAddCustomAssetType) {
      this.newCustomAssetType = this.getEmptyCustomAssetType();
    }
  }

  addNewCustomAssetType(): void {
    if (!this.newCustomAssetType.displayName) {
      this.showError('Please enter a display name');
      return;
    }

    this.configService.saveCustomAssetType(this.newCustomAssetType).subscribe({
      next: () => {
        this.showSuccess('Custom asset type added successfully');
        this.newCustomAssetType = this.getEmptyCustomAssetType();
        this.showAddCustomAssetType = false;
        this.loadCustomAssetTypes();
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to add custom asset type');
      }
    });
  }

  saveCustomAssetType(customType: CustomAssetType): void {
    this.configService.saveCustomAssetType(customType).subscribe({
      next: () => {
        this.showSuccess('Custom asset type saved successfully');
        this.editingCustomAssetType = null;
        this.loadCustomAssetTypes();
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to save custom asset type');
      }
    });
  }

  deleteCustomAssetType(id: number): void {
    Swal.fire({
      title: 'Are you sure?',
      text: 'Do you want to delete this custom asset type?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.configService.deleteCustomAssetType(id).subscribe({
          next: () => {
            Swal.fire('Deleted!', 'Custom asset type has been deleted.', 'success');
            this.loadCustomAssetTypes();
          },
          error: (err) => {
            Swal.fire('Error!', err?.error?.message || 'Failed to delete custom asset type', 'error');
          }
        });
      }
    });
  }

  // Custom Liability Type Methods
  loadCustomLiabilityTypes(): void {
    this.configService.getAllCustomLiabilityTypes().subscribe({
      next: (types) => {
        this.customLiabilityTypes = types;
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to load custom liability types');
      }
    });
  }

  toggleAddCustomLiabilityType(): void {
    this.showAddCustomLiabilityType = !this.showAddCustomLiabilityType;
    if (!this.showAddCustomLiabilityType) {
      this.newCustomLiabilityType = this.getEmptyCustomLiabilityType();
    }
  }

  addNewCustomLiabilityType(): void {
    if (!this.newCustomLiabilityType.displayName) {
      this.showError('Please enter a display name');
      return;
    }

    this.configService.saveCustomLiabilityType(this.newCustomLiabilityType).subscribe({
      next: () => {
        this.showSuccess('Custom liability type added successfully');
        this.newCustomLiabilityType = this.getEmptyCustomLiabilityType();
        this.showAddCustomLiabilityType = false;
        this.loadCustomLiabilityTypes();
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to add custom liability types');
      }
    });
  }

  saveCustomLiabilityType(customType: CustomLiabilityType): void {
    this.configService.saveCustomLiabilityType(customType).subscribe({
      next: () => {
        this.showSuccess('Custom liability type saved successfully');
        this.editingCustomLiabilityType = null;
        this.loadCustomLiabilityTypes();
      },
      error: (err) => {
        this.showError(err?.error?.message || 'Failed to save custom liability types');
      }
    });
  }

  deleteCustomLiabilityType(id: number): void {
    Swal.fire({
      title: 'Are you sure?',
      text: 'Do you want to delete this custom liability type?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.configService.deleteCustomLiabilityType(id).subscribe({
          next: () => {
            Swal.fire('Deleted!', 'Custom liability type has been deleted.', 'success');
            this.loadCustomLiabilityTypes();
          },
          error: (err) => {
            Swal.fire('Error!', err?.error?.message || 'Failed to delete custom liability type', 'error');
          }
        });
      }
    });
  }

  getEmptyCurrency(): CurrencyRate {
    return {
      currencyCode: '',
      currencyName: '',
      rateToInr: 1,
      isActive: true
    };
  }

  getEmptyCustomAssetType(): CustomAssetType {
    return {
      displayName: '',
      description: '',
      isActive: true
    };
  }

  getEmptyCustomLiabilityType(): CustomLiabilityType {
    return {
      displayName: '',
      description: '',
      isActive: true
    };
  }

  showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    setTimeout(() => this.errorMessage = '', 5000);
  }

  showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => this.successMessage = '', 3000);
  }
}
