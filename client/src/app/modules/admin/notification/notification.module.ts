import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificationRoutingModule } from './notification-routing.module';
import { NotificationComponent } from './notification.component';
import { NotificationService } from './notification.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { NotificationListComponent } from './notification-list/notification-list.component';
import { NotificationDtlModule } from './notification-dtl/notification-dtl.module';

@NgModule({
  declarations: [NotificationComponent, NotificationListComponent],
  imports: [CommonModule, NotificationRoutingModule, SharedModule, NotificationDtlModule],
  providers: [NotificationService]
})
export class NotificationModule {}
