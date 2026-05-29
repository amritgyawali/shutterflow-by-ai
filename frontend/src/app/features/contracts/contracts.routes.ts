import { Routes } from '@angular/router';
import { ContractListComponent } from './contract-list.component';
import { ContractDetailComponent } from './contract-detail.component';
import { ContractBuilderComponent } from './contract-builder.component';
import { authGuard } from '../../core/guards/auth.guard';

export const CONTRACTS_ROUTES: Routes = [
  { path: '', component: ContractListComponent, canActivate: [authGuard] },
  { path: 'builder', component: ContractBuilderComponent, canActivate: [authGuard] },
  { path: ':id', component: ContractDetailComponent, canActivate: [authGuard] }
];
