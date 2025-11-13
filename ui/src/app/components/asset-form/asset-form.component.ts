import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { ConfigurationService } from '../../services/configuration.service';
import { Asset } from '../../models/asset.model';
import { CurrencyRate, CustomAssetType } from '../../models/configuration.model';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-asset-form',
    templateUrl: './asset-form.component.html',
    styleUrls: ['./asset-form.component.css'],
    standalone: false
})
export class AssetFormComponent implements OnInit {
    asset: Asset = {
        name: '',
        customAssetTypeId: 0,
        currentValue: 0,
        currency: 'INR'
    };

    isEditMode = false;
    assetId?: number;
    loading = false;
    currencyOptions: CurrencyRate[] = [];
    assetTypeOptions: CustomAssetType[] = [];

    constructor(
        private assetService: AssetService,
        private router: Router,
        private route: ActivatedRoute,
        private configurationService: ConfigurationService
    ) {}

    ngOnInit(): void {
        this.configurationService.getAllCurrencyRates().subscribe({
            next: (currencies) => {
                this.currencyOptions = currencies.filter(c => c.isActive !== false);
            },
            error: (err) => {
                console.error('Failed to load currencies', err);
            }
        });
        this.configurationService.getAllCustomAssetTypes().subscribe({
            next: (types) => {
                this.assetTypeOptions = types.filter(t => t.isActive !== false);
            },
            error: (err) => {
                console.error('Failed to load asset types', err);
            }
        });
        this.route.params.subscribe(params => {
            if (params['id']) {
                this.isEditMode = true;
                this.assetId = +params['id'];
                this.loadAsset(this.assetId);
            }
        });
    }

    loadAsset(id: number): void {
        this.loading = true;
        this.assetService.getAssetById(id).subscribe({
            next: (asset) => {
                this.asset = asset;
                this.loading = false;
            },
            error: (error) => {
                console.error('Error loading asset:', error);
                Swal.fire('Error!', 'Failed to load asset', 'error');
                this.router.navigate(['/assets']);
            }
        });
    }

    onSubmit(): void {
        if (!this.asset.customAssetTypeId || this.asset.customAssetTypeId === 0) {
            Swal.fire('Error!', 'Please select an asset type', 'error');
            return;
        }

        this.loading = true;

        if (this.isEditMode && this.assetId) {
            this.assetService.updateAsset(this.assetId, this.asset).subscribe({
                next: () => {
                    this.loading = false;
                    Swal.fire('Success!', 'Asset updated successfully!', 'success');
                    this.router.navigate(['/assets']);
                },
                error: (error) => {
                    console.error('Error updating asset:', error);
                    Swal.fire('Error!', 'Failed to update asset', 'error');
                    this.loading = false;
                }
            });
        } else {
            this.assetService.createAsset(this.asset).subscribe({
                next: () => {
                    this.loading = false;
                    Swal.fire('Success!', 'Asset created successfully!', 'success');
                    this.router.navigate(['/assets']);
                },
                error: (error) => {
                    console.error('Error creating asset:', error);
                    Swal.fire('Error!', 'Failed to create asset', 'error');
                    this.loading = false;
                }
            });
        }
    }

    cancel(): void {
        this.router.navigate(['/assets']);
    }
}
