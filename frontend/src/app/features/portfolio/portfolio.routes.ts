import { Routes } from '@angular/router';
import { PortfolioEditorComponent } from './portfolio-editor.component';
import { LeadListComponent } from './lead-list.component';
import { authGuard } from '../../core/guards/auth.guard';

export const PORTFOLIO_ROUTES: Routes = [
  { path: '', component: PortfolioEditorComponent, canActivate: [authGuard] },
  { path: 'leads', component: LeadListComponent, canActivate: [authGuard] }
];
