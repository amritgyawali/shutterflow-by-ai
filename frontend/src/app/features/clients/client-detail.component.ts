import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-client-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <h1>Client Details</h1>
      <p>Feature module loaded successfully.</p>
    </div>
  `
})
export class ClientDetailComponent {}
