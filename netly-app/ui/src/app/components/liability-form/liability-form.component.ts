import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LiabilityService } from '../../services/liability.service';
import { Liability } from '../../models/liability.model';
import { ConfigurationService } from '../../services/configuration.service';
import { CurrencyRate, CustomLiabilityType } from '../../models/configuration.model';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-liability-form',
    templateUrl: './liability-form.component.html',
    styleUrls: ['./liability-form.component.css'],
    standalone: false
})
export class LiabilityFormComponent implements OnInit {
  liability: Liability = {
    name: '',
    customLiabilityTypeId: 0,
    currentBalance: 0,
    currency: 'INR'
  };

  isEditMode = false;
  liabilityId?: number;
  loading = false;
  currencyOptions: CurrencyRate[] = [];
  liabilityTypeOptions: CustomLiabilityType[] = [];

  constructor(
    private liabilityService: LiabilityService,
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
    this.configurationService.getAllCustomLiabilityTypes().subscribe({
      next: (types) => {
        this.liabilityTypeOptions = types.filter(t => t.isActive !== false);
      },
      error: (err) => {
        console.error('Failed to load liability types', err);
      }
    });
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.liabilityId = +params['id'];
        this.loadLiability(this.liabilityId);
      }
    });
  }

  loadLiability(id: number): void {
    this.loading = true;
    this.liabilityService.getLiabilityById(id).subscribe({
      next: (liability) => {
        this.liability = liability;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading liability:', error);
        Swal.fire('Error!', 'Failed to load liability', 'error');
        this.router.navigate(['/liabilities']);
      }
    });
  }

  onSubmit(): void {
    if (!this.liability.customLiabilityTypeId || this.liability.customLiabilityTypeId === 0) {
      Swal.fire('Error!', 'Please select a liability type', 'error');
      return;
    }

    this.loading = true;

    if (this.isEditMode && this.liabilityId) {
      this.liabilityService.updateLiability(this.liabilityId, this.liability).subscribe({
        next: () => {
          this.loading = false;
          Swal.fire('Success!', 'Liability updated successfully!', 'success');
          this.router.navigate(['/liabilities']);
        },
        error: (error) => {
          console.error('Error updating liability:', error);
          Swal.fire('Error!', 'Failed to update liability', 'error');
          this.loading = false;
        }
      });
    } else {
      this.liabilityService.createLiability(this.liability).subscribe({
        next: () => {
          this.loading = false;
          Swal.fire('Success!', 'Liability created successfully!', 'success');
          this.router.navigate(['/liabilities']);
        },
        error: (error) => {
          console.error('Error creating liability:', error);
          Swal.fire('Error!', 'Failed to create liability', 'error');
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/liabilities']);
  }
}
