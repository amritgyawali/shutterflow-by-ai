import { Routes } from '@angular/router';
import { PaymentListComponent } from './payment-list.component';
import { authGuard } from '../../core/guards/auth.guard';

export const PAYMENTS_ROUTES: Routes = [
  { path: '', component: PaymentListComponent, canActivate: [authGuard] }
];
