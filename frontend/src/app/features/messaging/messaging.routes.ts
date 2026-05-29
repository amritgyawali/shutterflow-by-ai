import { Routes } from '@angular/router';
import { MessageListComponent } from './message-list.component';
import { ConversationComponent } from './conversation.component';
import { authGuard } from '../../core/guards/auth.guard';

export const MESSAGING_ROUTES: Routes = [
  { path: '', component: MessageListComponent, canActivate: [authGuard] },
  { path: ':id', component: ConversationComponent, canActivate: [authGuard] }
];
