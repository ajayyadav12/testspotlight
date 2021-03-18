import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProcessDtlNotificationsAlertsComponent } from './process-dtl-notifications-alerts.component';
import { SharedModule } from 'src/app/shared/shared.module';



@NgModule({
  declarations: [ProcessDtlNotificationsAlertsComponent],
  imports: [
    CommonModule, SharedModule
  ],
  exports: [ProcessDtlNotificationsAlertsComponent]
})
export class ProcessDtlNotificationsAlertsModule { }
