import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-portfolio-editor',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-container">
      <h1>Portfolio Editor</h1>
      <p>Feature module loaded successfully.</p>
    </div>
  `
})
export class PortfolioEditorComponent {}
