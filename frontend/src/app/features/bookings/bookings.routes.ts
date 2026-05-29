import { Routes } from '@angular/router';
import { BookingListComponent } from './booking-list.component';
import { BookingDetailComponent } from './booking-detail.component';
import { BookingCreateComponent } from './booking-create.component';
import { authGuard } from '../../core/guards/auth.guard';

export const BOOKINGS_ROUTES: Routes = [
  { path: '', component: BookingListComponent, canActivate: [authGuard] },
  { path: 'new', component: BookingCreateComponent, canActivate: [authGuard] },
  { path: ':id', component: BookingDetailComponent, canActivate: [authGuard] }
];
