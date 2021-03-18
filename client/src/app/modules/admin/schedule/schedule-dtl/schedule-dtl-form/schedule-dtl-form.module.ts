import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScheduleDtlFormComponent } from './schedule-dtl-form.component';
import { ScheduleDtlWeeklyModule } from '../schedule-dtl-weekly/schedule-dtl-weekly.module';
import { ScheduleDtlMonthlyModule } from '../schedule-dtl-monthly/schedule-dtl-monthly.module';
import { ScheduleDtlCustomModule } from '../schedule-dtl-custom/schedule-dtl-custom.module';
import { ScheduleDtlDailyComponent } from '../schedule-dtl-daily/schedule-dtl-daily.component';
import { SharedModule } from 'src/app/shared/shared.module';



@NgModule({
  declarations: [ScheduleDtlFormComponent, ScheduleDtlDailyComponent],
  imports: [
    CommonModule,
    SharedModule,
    ScheduleDtlWeeklyModule,
    ScheduleDtlMonthlyModule,
    ScheduleDtlCustomModule
  ],
  exports: [ScheduleDtlFormComponent]
})
export class ScheduleDtlFormModule { }
