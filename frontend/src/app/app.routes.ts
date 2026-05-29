import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
  },
  {
    path: 'bookings',
    loadChildren: () => import('./features/bookings/bookings.routes').then(m => m.BOOKINGS_ROUTES)
  },
  {
    path: 'calendar',
    loadChildren: () => import('./features/calendar/calendar.routes').then(m => m.CALENDAR_ROUTES)
  },
  {
    path: 'clients',
    loadChildren: () => import('./features/clients/clients.routes').then(m => m.CLIENTS_ROUTES)
  },
  {
    path: 'contracts',
    loadChildren: () => import('./features/contracts/contracts.routes').then(m => m.CONTRACTS_ROUTES)
  },
  {
    path: 'invoices',
    loadChildren: () => import('./features/invoices/invoices.routes').then(m => m.INVOICES_ROUTES)
  },
  {
    path: 'payments',
    loadChildren: () => import('./features/payments/payments.routes').then(m => m.PAYMENTS_ROUTES)
  },
  {
    path: 'galleries',
    loadChildren: () => import('./features/galleries/galleries.routes').then(m => m.GALLERIES_ROUTES)
  },
  {
    path: 'messaging',
    loadChildren: () => import('./features/messaging/messaging.routes').then(m => m.MESSAGING_ROUTES)
  },
  {
    path: 'reviews',
    loadChildren: () => import('./features/reviews/reviews.routes').then(m => m.REVIEWS_ROUTES)
  },
  {
    path: 'settings',
    loadChildren: () => import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
  },
  {
    path: 'portfolio',
    loadChildren: () => import('./features/portfolio/portfolio.routes').then(m => m.PORTFOLIO_ROUTES)
  },
  { path: '**', redirectTo: '/dashboard' }
];
