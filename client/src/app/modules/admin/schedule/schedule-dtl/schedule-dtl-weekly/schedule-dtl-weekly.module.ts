import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScheduleDtlWeeklyComponent } from './schedule-dtl-weekly.component';
import { SharedModule } from 'src/app/shared/shared.module';



@NgModule({
  declarations: [ScheduleDtlWeeklyComponent],
  imports: [
    CommonModule, SharedModule
  ],
  exports: [ScheduleDtlWeeklyComponent]
})
export class ScheduleDtlWeeklyModule { }
