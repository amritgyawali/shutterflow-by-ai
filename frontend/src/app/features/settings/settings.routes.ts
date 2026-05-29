import { Routes } from '@angular/router';
import { SettingsComponent } from './settings.component';
import { authGuard } from '../../core/guards/auth.guard';

export const SETTINGS_ROUTES: Routes = [
  { path: '', component: SettingsComponent, canActivate: [authGuard] }
];
