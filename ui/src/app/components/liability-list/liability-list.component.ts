import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LiabilityService } from '../../services/liability.service';
import { Liability } from '../../models/liability.model';
import { ConfigurationService } from '../../services/configuration.service';
import { CustomLiabilityType } from '../../models/configuration.model';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-liability-list',
    templateUrl: './liability-list.component.html',
    styleUrls: ['./liability-list.component.css'],
    standalone: false
})
export class LiabilityListComponent implements OnInit {
    liabilities: Liability[] = [];
    filteredLiabilities: Liability[] = [];
    loading = true;
    filterType: string = 'ALL';
    filterStatus: string = 'ALL';
    searchTerm: string = '';

    // Summary statistics
    totalCurrentBalance: number = 0;
    totalOriginalAmount: number = 0;
    totalPaidAmount: number = 0;
    overallPaidOffPercentage: number = 0;

    liabilityTypeOptions: CustomLiabilityType[] = [];

    constructor(
        private liabilityService: LiabilityService,
        private router: Router,
        private configurationService: ConfigurationService
    ) {}

    ngOnInit(): void {
        this.configurationService.getAllCustomLiabilityTypes().subscribe({
            next: (types) => {
                this.liabilityTypeOptions = types;
            },
            error: (err) => {
                console.error('Failed to load liability types', err);
            }
        });
        this.loadLiabilities();
    }

    loadLiabilities(): void {
        this.loading = true;
        this.liabilityService.getAllLiabilities().subscribe({
            next: (liabilities) => {
                this.liabilities = liabilities;
                this.applyFilters();
                this.loading = false;
            },
            error: (error) => {
                console.error('Error loading liabilities:', error);
                this.loading = false;
            }
        });
    }

    applyFilters(): void {
        this.filteredLiabilities = this.liabilities.filter(liability => {
            const matchesType = this.filterType === 'ALL' || liability.liabilityTypeDisplayName === this.filterType;
            const matchesSearch = liability.name.toLowerCase().includes(this.searchTerm.toLowerCase());

            // Filter by status
            let matchesStatus = true;
            if (this.filterStatus === 'ACTIVE') {
                matchesStatus = (liability.currentBalance ?? 0) > 0;
            } else if (this.filterStatus === 'CLOSED') {
                matchesStatus = (liability.currentBalance ?? 0) === 0;
            }
            // If 'ALL', matchesStatus remains true

            return matchesType && matchesSearch && matchesStatus;
        });
        this.calculateSummaryStatistics();
    }

    // Helper method to check if a liability is closed
    isClosed(liability: Liability): boolean {
        return (liability.currentBalance ?? 0) === 0;
    }

    calculateSummaryStatistics(): void {
        this.totalCurrentBalance = 0;
        this.totalOriginalAmount = 0;
        this.totalPaidAmount = 0;
        this.overallPaidOffPercentage = 0;

        this.filteredLiabilities.forEach(liability => {
            // All amounts are in INR
            this.totalCurrentBalance += (liability.currentBalance || 0);
            this.totalOriginalAmount += (liability.originalAmount || 0);
        });

        // Calculate total paid amount
        this.totalPaidAmount = this.totalOriginalAmount - this.totalCurrentBalance;

        // Calculate overall paid off percentage
        this.overallPaidOffPercentage = this.totalOriginalAmount > 0
            ? (this.totalPaidAmount / this.totalOriginalAmount) * 100
            : 0;
    }

    onFilterChange(): void {
        this.applyFilters();
    }

    onSearchChange(): void {
        this.applyFilters();
    }

    editLiability(liability: Liability): void {
        this.router.navigate(['/liabilities/edit', liability.id]);
    }

    deleteLiability(liability: Liability): void {
        Swal.fire({
            title: 'Are you sure?',
            text: `Do you want to delete ${liability.name}?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, delete it!'
        }).then((result) => {
            if (result.isConfirmed) {
                this.liabilityService.deleteLiability(liability.id!).subscribe({
                    next: () => {
                        Swal.fire('Deleted!', 'Liability has been deleted.', 'success');
                        this.loadLiabilities();
                    },
                    error: (error) => {
                        console.error('Error deleting liability:', error);
                        Swal.fire('Error!', 'Failed to delete liability', 'error');
                    }
                });
            }
        });
    }

    addNewLiability(): void {
        this.router.navigate(['/liabilities/new']);
    }

    formatDate(date: string | undefined): string {
        if (!date) return 'N/A';
        return new Date(date).toLocaleDateString();
    }

    formatPercentage(value: number | undefined): string {
        if (!value) return '0.00%';
        return `${value.toFixed(2)}%`;
    }
}

