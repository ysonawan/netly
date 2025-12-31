import { Component, OnInit } from '@angular/core';
import { ConfigurationService } from '../../services/configuration.service';
import { CustomAssetType, CustomLiabilityType } from '../../models/configuration.model';
import Swal from 'sweetalert2';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-settings',
  standalone: false,
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  activeTab: 'asset-types' | 'liability-types' = 'asset-types';

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

  constructor(
    private configService: ConfigurationService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadAllConfigurations();
  }

  loadAllConfigurations(): void {
    this.loadCustomAssetTypes();
    this.loadCustomLiabilityTypes();
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
    this.toastr.error(message);
  }

  showSuccess(message: string): void {
    this.toastr.success(message);
  }
}
