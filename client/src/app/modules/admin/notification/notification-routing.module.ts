import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { NotificationListComponent } from './notification-list/notification-list.component';
import { NotificationDtlComponent } from './notification-dtl/notification-dtl.component';
import { NotificationComponent } from './notification.component';

const routes: Routes = [
  {
    path: '',
    component: NotificationComponent,
    children: [{ path: '', component: NotificationListComponent }, { path: ':id', component: NotificationDtlComponent }]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationRoutingModule {}
