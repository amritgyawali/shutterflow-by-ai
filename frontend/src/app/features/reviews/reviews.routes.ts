import { Routes } from '@angular/router';
import { ReviewListComponent } from './review-list.component';
import { authGuard } from '../../core/guards/auth.guard';

export const REVIEWS_ROUTES: Routes = [
  { path: '', component: ReviewListComponent, canActivate: [authGuard] }
];
