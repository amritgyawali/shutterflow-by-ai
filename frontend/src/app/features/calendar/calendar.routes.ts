import { Routes } from '@angular/router';
import { CalendarViewComponent } from './calendar-view.component';
import { authGuard } from '../../core/guards/auth.guard';

export const CALENDAR_ROUTES: Routes = [
  { path: '', component: CalendarViewComponent, canActivate: [authGuard] }
];
