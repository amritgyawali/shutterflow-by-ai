import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-lead-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <h1>Leads</h1>
      <p>Feature module loaded successfully.</p>
    </div>
  `
})
export class LeadListComponent {}
