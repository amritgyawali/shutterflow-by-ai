import { Routes } from '@angular/router';
import { InvoiceListComponent } from './invoice-list.component';
import { InvoiceDetailComponent } from './invoice-detail.component';
import { InvoiceCreateComponent } from './invoice-create.component';
import { authGuard } from '../../core/guards/auth.guard';

export const INVOICES_ROUTES: Routes = [
  { path: '', component: InvoiceListComponent, canActivate: [authGuard] },
  { path: 'new', component: InvoiceCreateComponent, canActivate: [authGuard] },
  { path: ':id', component: InvoiceDetailComponent, canActivate: [authGuard] }
];
