import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <h1>Invoice Details</h1>
      <p>Feature module loaded successfully.</p>
    </div>
  `
})
export class InvoiceDetailComponent {}
