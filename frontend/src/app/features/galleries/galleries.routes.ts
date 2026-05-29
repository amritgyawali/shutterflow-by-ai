import { Routes } from '@angular/router';
import { GalleryListComponent } from './gallery-list.component';
import { GalleryDetailComponent } from './gallery-detail.component';
import { GalleryUploadComponent } from './gallery-upload.component';
import { authGuard } from '../../core/guards/auth.guard';

export const GALLERIES_ROUTES: Routes = [
  { path: '', component: GalleryListComponent, canActivate: [authGuard] },
  { path: 'upload', component: GalleryUploadComponent, canActivate: [authGuard] },
  { path: ':id', component: GalleryDetailComponent, canActivate: [authGuard] }
];
