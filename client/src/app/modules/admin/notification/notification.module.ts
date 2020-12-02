import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificationRoutingModule } from './notification-routing.module';
import { NotificationComponent } from './notification.component';
import { NotificationService } from './notification.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule } from '@angular/forms';
import { NotificationListComponent } from './notification-list/notification-list.component';
import { NotificationDtlComponent } from './notification-dtl/notification-dtl.component';

@NgModule({
  declarations: [NotificationComponent, NotificationListComponent, NotificationDtlComponent],
  imports: [CommonModule, NotificationRoutingModule, SharedModule, ReactiveFormsModule],
  providers: [NotificationService]
})
export class NotificationModule {}
