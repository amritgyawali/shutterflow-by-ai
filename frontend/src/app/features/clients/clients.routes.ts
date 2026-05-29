import { Routes } from '@angular/router';
import { ClientListComponent } from './client-list.component';
import { ClientDetailComponent } from './client-detail.component';
import { authGuard } from '../../core/guards/auth.guard';

export const CLIENTS_ROUTES: Routes = [
  { path: '', component: ClientListComponent, canActivate: [authGuard] },
  { path: ':id', component: ClientDetailComponent, canActivate: [authGuard] }
];
