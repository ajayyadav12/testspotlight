import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { NotificationDtlComponent } from './notification-dtl.component';



@NgModule({
  declarations: [NotificationDtlComponent],
  imports: [
    CommonModule, SharedModule
  ],
  exports: [NotificationDtlComponent]
})
export class NotificationDtlModule { }
