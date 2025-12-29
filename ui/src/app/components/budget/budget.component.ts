import { Component, OnInit, HostListener } from '@angular/core';
import { BudgetService } from '../../services/budget.service';
import { BudgetItem, BudgetSummary } from '../../models/budget.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-budget',
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css'],
  standalone: false
})
export class BudgetComponent implements OnInit {
  incomeItems: BudgetItem[] = [];
  expenseItems: BudgetItem[] = [];
  summary: BudgetSummary = {
    totalIncome: 0,
    totalExpenses: 0,
    totalInvestments: 0,
    totalNonInvestmentExpenses: 0,
    totalSurplus: 0,
    investmentPercentage: 0,
    nonInvestmentExpensePercentage: 0,
    savingsRate: 0
  };
  loading = true;

  // Chart options
  incomeVsExpensesChartOptions: any = {};
  expenseBreakdownChartOptions: any = {};
  savingsGaugeChartOptions: any = {};
  topExpensesChartOptions: any = {};
  incomeBreakdownChartOptions: any = {};
  hasChartData = false;

  // Edit mode tracking
  editingIncome: { [key: number]: boolean } = {};
  editingExpense: { [key: number]: boolean } = {};

  // Add new item tracking
  addingIncome = false;
  addingExpense = false;

  // New item models
  newIncomeItem: BudgetItem = this.createEmptyIncomeItem();
  newExpenseItem: BudgetItem = this.createEmptyExpenseItem();

  constructor(private budgetService: BudgetService) {}

  @HostListener('window:resize')
  onResize(): void {
    // Refresh chart options on resize to adapt to new viewport size
    this.prepareChartData();
  }

  ngOnInit(): void {
    this.loadBudgetData();
  }

  loadBudgetData(): void {
    this.loading = true;
    this.budgetService.getAllBudgetItems().subscribe({
      next: (items) => {
        this.incomeItems = items.filter(item => item.itemType === 'INCOME')
          .sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
        this.expenseItems = items.filter(item => item.itemType === 'EXPENSE')
          .sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
        this.loadSummary();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading budget items:', error);
        Swal.fire('Error', 'Failed to load budget data', 'error');
        this.loading = false;
      }
    });
  }

  loadSummary(): void {
    this.budgetService.getBudgetSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.prepareChartData();
      },
      error: (error) => {
        console.error('Error loading summary:', error);
      }
    });
  }

  createEmptyIncomeItem(): BudgetItem {
    return {
      itemType: 'INCOME',
      itemName: '',
      amount: 0,
      isInvestment: false,
      description: ''
    };
  }

  createEmptyExpenseItem(): BudgetItem {
    return {
      itemType: 'EXPENSE',
      itemName: '',
      amount: 0,
      isInvestment: false,
      description: ''
    };
  }

  // Add new item functions
  startAddingIncome(): void {
    this.addingIncome = true;
    this.newIncomeItem = this.createEmptyIncomeItem();
  }

  startAddingExpense(): void {
    this.addingExpense = true;
    this.newExpenseItem = this.createEmptyExpenseItem();
  }

  cancelAddingIncome(): void {
    this.addingIncome = false;
    this.newIncomeItem = this.createEmptyIncomeItem();
  }

  cancelAddingExpense(): void {
    this.addingExpense = false;
    this.newExpenseItem = this.createEmptyExpenseItem();
  }

  saveNewIncome(): void {
    if (!this.newIncomeItem.itemName || this.newIncomeItem.amount <= 0) {
      Swal.fire('Validation Error', 'Please provide a name and valid amount', 'warning');
      return;
    }

    this.budgetService.createBudgetItem(this.newIncomeItem).subscribe({
      next: () => {
        this.addingIncome = false;
        this.newIncomeItem = this.createEmptyIncomeItem();
        this.loadBudgetData();
        Swal.fire('Success', 'Income item added successfully', 'success');
      },
      error: (error) => {
        console.error('Error creating income item:', error);
        Swal.fire('Error', 'Failed to add income item', 'error');
      }
    });
  }

  saveNewExpense(): void {
    if (!this.newExpenseItem.itemName || this.newExpenseItem.amount <= 0) {
      Swal.fire('Validation Error', 'Please provide a name and valid amount', 'warning');
      return;
    }

    this.budgetService.createBudgetItem(this.newExpenseItem).subscribe({
      next: () => {
        this.addingExpense = false;
        this.newExpenseItem = this.createEmptyExpenseItem();
        this.loadBudgetData();
        Swal.fire('Success', 'Expense item added successfully', 'success');
      },
      error: (error) => {
        console.error('Error creating expense item:', error);
        Swal.fire('Error', 'Failed to add expense item', 'error');
      }
    });
  }

  // Edit functions
  startEditingIncome(item: BudgetItem): void {
    if (item.id) {
      this.editingIncome[item.id] = true;
    }
  }

  startEditingExpense(item: BudgetItem): void {
    if (item.id) {
      this.editingExpense[item.id] = true;
    }
  }

  cancelEditingIncome(item: BudgetItem): void {
    if (item.id) {
      this.editingIncome[item.id] = false;
      this.loadBudgetData();
    }
  }

  cancelEditingExpense(item: BudgetItem): void {
    if (item.id) {
      this.editingExpense[item.id] = false;
      this.loadBudgetData();
    }
  }

  saveIncome(item: BudgetItem): void {
    if (!item.id) return;

    if (!item.itemName || item.amount <= 0) {
      Swal.fire('Validation Error', 'Please provide a name and valid amount', 'warning');
      return;
    }

    this.budgetService.updateBudgetItem(item.id, item).subscribe({
      next: () => {
        this.editingIncome[item.id!] = false;
        this.loadBudgetData();
        Swal.fire('Success', 'Income item updated successfully', 'success');
      },
      error: (error) => {
        console.error('Error updating income item:', error);
        Swal.fire('Error', 'Failed to update income item', 'error');
      }
    });
  }

  saveExpense(item: BudgetItem): void {
    if (!item.id) return;

    if (!item.itemName || item.amount <= 0) {
      Swal.fire('Validation Error', 'Please provide a name and valid amount', 'warning');
      return;
    }

    this.budgetService.updateBudgetItem(item.id, item).subscribe({
      next: () => {
        this.editingExpense[item.id!] = false;
        this.loadBudgetData();
        Swal.fire('Success', 'Expense item updated successfully', 'success');
      },
      error: (error) => {
        console.error('Error updating expense item:', error);
        Swal.fire('Error', 'Failed to update expense item', 'error');
      }
    });
  }

  deleteIncome(item: BudgetItem): void {
    if (!item.id) return;

    Swal.fire({
      title: 'Are you sure?',
      text: 'Do you want to delete this income item?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed && item.id) {
        this.budgetService.deleteBudgetItem(item.id).subscribe({
          next: () => {
            this.loadBudgetData();
            Swal.fire('Deleted!', 'Income item has been deleted.', 'success');
          },
          error: (error) => {
            console.error('Error deleting income item:', error);
            Swal.fire('Error', 'Failed to delete income item', 'error');
          }
        });
      }
    });
  }

  deleteExpense(item: BudgetItem): void {
    if (!item.id) return;

    Swal.fire({
      title: 'Are you sure?',
      text: 'Do you want to delete this expense item?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed && item.id) {
        this.budgetService.deleteBudgetItem(item.id).subscribe({
          next: () => {
            this.loadBudgetData();
            Swal.fire('Deleted!', 'Expense item has been deleted.', 'success');
          },
          error: (error) => {
            console.error('Error deleting expense item:', error);
            Swal.fire('Error', 'Failed to delete expense item', 'error');
          }
        });
      }
    });
  }

  isEditingIncome(item: BudgetItem): boolean {
    return item.id ? this.editingIncome[item.id] || false : false;
  }

  isEditingExpense(item: BudgetItem): boolean {
    return item.id ? this.editingExpense[item.id] || false : false;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  formatCompactNumber(value: number): string {
    if (value >= 10000000) {
      return (value / 10000000).toFixed(1) + ' Cr';
    } else if (value >= 100000) {
      return (value / 100000).toFixed(1) + ' L';
    } else if (value >= 1000) {
      return (value / 1000).toFixed(1) + ' K';
    }
    return value.toString();
  }

  getInvestmentExpenses(): BudgetItem[] {
    return this.expenseItems.filter(item => item.isInvestment);
  }

  getNonInvestmentExpenses(): BudgetItem[] {
    return this.expenseItems.filter(item => !item.isInvestment);
  }

  prepareChartData(): void {
    this.hasChartData = this.incomeItems.length > 0 || this.expenseItems.length > 0;

    if (!this.hasChartData) return;

    this.prepareIncomeVsExpensesChart();
    this.prepareExpenseBreakdownChart();
    this.prepareSavingsGaugeChart();
    this.prepareTopExpensesChart();
    this.prepareIncomeBreakdownChart();
  }

  prepareIncomeVsExpensesChart(): void {
    const isMobile = window.innerWidth < 640;

    this.incomeVsExpensesChartOptions = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params: any) => {
          let tooltip = `<div style="font-weight: bold; margin-bottom: 5px;">${params[0].axisValue}</div>`;
          params.forEach((param: any) => {
            tooltip += `<div>${param.marker} ${param.seriesName}: ${this.formatCurrency(param.value)}</div>`;
          });
          return tooltip;
        }
      },
      legend: {
        data: ['Income', 'Expenses', 'Surplus/Deficit'],
        bottom: 0,
        textStyle: {
          fontSize: isMobile ? 10 : 12
        },
        tooltip: {
          show: true,
          formatter: (params: any) => {
            if (params.name === 'Income') {
              return `Income<br/>${this.formatCurrency(this.summary.totalIncome)}<br/>All your monthly income sources`;
            } else if (params.name === 'Expenses') {
              return `Expenses<br/>${this.formatCurrency(this.summary.totalExpenses)}<br/>Total of all expenses`;
            } else if (params.name === 'Surplus/Deficit') {
              return `Surplus/Deficit<br/>${this.formatCurrency(this.summary.totalSurplus)}<br/>${this.summary.totalSurplus >= 0 ? 'Your potential savings' : 'Expenses exceed income'}`;
            }
            return params.name;
          }
        }
      },
      grid: {
        left: isMobile ? '15%' : '10%',
        right: isMobile ? '5%' : '10%',
        top: '10%',
        bottom: isMobile ? '15%' : '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['Monthly Budget'],
        axisLabel: {
          fontSize: isMobile ? 10 : 12
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          fontSize: isMobile ? 10 : 11,
          formatter: (value: number) => {
            return this.formatCompactNumber(value);
          }
        }
      },
      series: [
        {
          name: 'Income',
          type: 'bar',
          data: [this.summary.totalIncome],
          itemStyle: {
            color: '#10b981'
          },
          label: {
            show: !isMobile,
            position: 'top',
            formatter: (params: any) => {
              return this.formatCompactNumber(params.value);
            }
          }
        },
        {
          name: 'Expenses',
          type: 'bar',
          data: [this.summary.totalExpenses],
          itemStyle: {
            color: '#ef4444'
          },
          label: {
            show: !isMobile,
            position: 'top',
            formatter: (params: any) => {
              return this.formatCompactNumber(params.value);
            }
          }
        },
        {
          name: 'Surplus/Deficit',
          type: 'bar',
          data: [this.summary.totalSurplus],
          itemStyle: {
            color: this.summary.totalSurplus >= 0 ? '#059669' : '#dc2626'
          },
          label: {
            show: !isMobile,
            position: this.summary.totalSurplus >= 0 ? 'top' : 'bottom',
            formatter: (params: any) => {
              return this.formatCompactNumber(params.value);
            }
          }
        }
      ]
    };
  }

  prepareExpenseBreakdownChart(): void {
    const isMobile = window.innerWidth < 640;

    const data = [
      {
        name: 'Investments',
        value: this.summary.totalInvestments,
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: 'Living Expenses',
        value: this.summary.totalNonInvestmentExpenses,
        itemStyle: { color: '#f97316' }
      }
    ].filter(item => item.value > 0);

    this.expenseBreakdownChartOptions = {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          return `${params.name}<br/>${this.formatCurrency(params.value)} (${params.percent}%)`;
        }
      },
      legend: {
        orient: isMobile ? 'horizontal' : 'vertical',
        bottom: isMobile ? 0 : undefined,
        right: isMobile ? undefined : 10,
        top: isMobile ? undefined : 'center',
        left: isMobile ? 'center' : undefined,
        textStyle: {
          fontSize: isMobile ? 10 : 12
        },
        tooltip: {
          show: true,
          formatter: (params: any) => {
            const dataItem = data.find((item: any) => item.name === params.name);
            if (dataItem) {
              const total = this.summary.totalExpenses;
              const percent = ((dataItem.value / total) * 100).toFixed(1);
              const formattedValue = this.formatCurrency(dataItem.value);
              if (params.name === 'Investments') {
                return `Investments<br/>${formattedValue} (${percent}%)<br/>SIP, EPF, NPS, etc.`;
              } else if (params.name === 'Living Expenses') {
                return `Living Expenses<br/>${formattedValue} (${percent}%)<br/>Rent, groceries, utilities, etc.`;
              }
              return `${params.name}<br/>${formattedValue} (${percent}%)`;
            }
            return params.name;
          }
        }
      },
      series: [
        {
          name: 'Expense Type',
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
            show: !isMobile,
            formatter: '{b}: {d}%'
          }
        }
      ]
    };
  }

  prepareSavingsGaugeChart(): void {
    const isMobile = window.innerWidth < 640;
    //const savingsRate = this.summary.savingsRate;
    const investmentPercentage = this.summary.investmentPercentage;

    this.savingsGaugeChartOptions = {
      tooltip: {
        formatter: () => {
          return `Savings Rate: ${investmentPercentage.toFixed(1)}%`;
        }
      },
      series: [
        {
          type: 'gauge',
          radius: isMobile ? '80%' : '90%',
          startAngle: 180,
          endAngle: 0,
          min: -50,
          max: 100,
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              width: isMobile ? 20 : 30,
              color: [
                [0.2, '#dc2626'],
                [0.4, '#f97316'],
                [0.6, '#fbbf24'],
                [0.8, '#10b981'],
                [1, '#059669']
              ]
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            },
            width: isMobile ? 4 : 6
          },
          axisTick: {
            distance: isMobile ? -20 : -30,
            length: isMobile ? 5 : 8,
            lineStyle: {
              color: '#fff',
              width: 2
            }
          },
          splitLine: {
            distance: isMobile ? -20 : -30,
            length: isMobile ? 10 : 15,
            lineStyle: {
              color: '#fff',
              width: 3
            }
          },
          axisLabel: {
            color: 'auto',
            distance: isMobile ? 25 : 40,
            fontSize: isMobile ? 8 : 10,
            formatter: (value: number) => {
              if (value === -50) return '-50%';
              if (value === 0) return '0%';
              if (value === 25) return '25%';
              if (value === 50) return '50%';
              if (value === 75) return '75%';
              if (value === 100) return '100%';
              return '';
            }
          },
          detail: {
            fontSize: isMobile ? 16 : 24,
            offsetCenter: [0, isMobile ? '50%' : '55%'],
            valueAnimation: true,
            formatter: (value: number) => {
              return value.toFixed(1) + '%';
            },
            color: 'auto'
          },
          data: [
            {
              value: investmentPercentage,
              name: 'Savings Rate'
            }
          ],
          title: {
            fontSize: isMobile ? 10 : 14,
            offsetCenter: [0, isMobile ? '70%' : '75%']
          }
        }
      ],

    };
  }

  prepareTopExpensesChart(): void {
    const isMobile = window.innerWidth < 640;

    // Get top 10 expenses by amount
    const topExpenses = [...this.expenseItems]
      .sort((a, b) => b.amount - a.amount)
      .slice(0, 10);

    const expenseNames = topExpenses.map(item => item.itemName);
    const expenseValues = topExpenses.map(item => item.amount);
    const expenseColors = topExpenses.map(item => item.isInvestment ? '#3b82f6' : '#f97316');

    this.topExpensesChartOptions = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params: any) => {
          const item = topExpenses[params[0].dataIndex];
          return `${params[0].name}<br/>${this.formatCurrency(params[0].value)}<br/>${item.isInvestment ? '(Investment)' : '(Living Expense)'}`;
        }
      },
      grid: {
        left: isMobile ? '25%' : '20%',
        right: '10%',
        top: '10%',
        bottom: '10%',
        containLabel: false
      },
      xAxis: {
        type: 'value',
        axisLabel: {
          fontSize: isMobile ? 9 : 11,
          formatter: (value: number) => {
            return this.formatCompactNumber(value);
          }
        }
      },
      yAxis: {
        type: 'category',
        data: expenseNames,
        axisLabel: {
          fontSize: isMobile ? 9 : 11,
          formatter: (value: string) => {
            return value.length > (isMobile ? 15 : 20) ? value.substring(0, isMobile ? 12 : 17) + '...' : value;
          }
        }
      },
      series: [
        {
          name: 'Amount',
          type: 'bar',
          data: expenseValues.map((value, index) => ({
            value: value,
            itemStyle: { color: expenseColors[index] }
          })),
          label: {
            show: !isMobile,
            position: 'right',
            formatter: (params: any) => {
              return this.formatCompactNumber(params.value);
            },
            fontSize: 10
          }
        }
      ]
    };
  }

  prepareIncomeBreakdownChart(): void {
    const isMobile = window.innerWidth < 640;

    // Prepare income items data with colors
    const incomeData = this.incomeItems.map((item, index) => ({
      name: item.itemName,
      value: item.amount,
      itemStyle: { color: this.getColorForIndex(index) }
    }));

    this.incomeBreakdownChartOptions = {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          return `${params.name}<br/>${this.formatCurrency(params.value)} (${params.percent}%)`;
        }
      },
      legend: {
        orient: isMobile ? 'horizontal' : 'vertical',
        bottom: isMobile ? 0 : undefined,
        right: isMobile ? undefined : 10,
        top: isMobile ? undefined : 'center',
        left: isMobile ? 'center' : undefined,
        textStyle: {
          fontSize: isMobile ? 10 : 12
        },
        type: 'scroll',
        pageIconSize: isMobile ? 10 : 12,
        tooltip: {
          show: true,
          formatter: (params: any) => {
            const dataItem = incomeData.find((item: any) => item.name === params.name);
            if (dataItem) {
              const total = this.summary.totalIncome;
              const percent = ((dataItem.value / total) * 100).toFixed(1);
              const formattedValue = this.formatCurrency(dataItem.value);
              return `${params.name}<br/>${formattedValue} (${percent}%)<br/>Income source`;
            }
            return params.name;
          }
        }
      },
      series: [
        {
          name: 'Income Sources',
          type: 'pie',
          radius: isMobile ? '50%' : '65%',
          center: isMobile ? ['50%', '42%'] : ['40%', '50%'],
          data: incomeData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            show: !isMobile,
            formatter: '{b}: {d}%'
          }
        }
      ]
    };
  }

  getColorForIndex(index: number): string {
    const colors = [
      '#3b82f6', // blue
      '#10b981', // green
      '#f59e0b', // amber
      '#ef4444', // red
      '#8b5cf6', // purple
      '#ec4899', // pink
      '#06b6d4', // cyan
      '#84cc16', // lime
      '#f97316', // orange
      '#6366f1'  // indigo
    ];
    return colors[index % colors.length];
  }
}
