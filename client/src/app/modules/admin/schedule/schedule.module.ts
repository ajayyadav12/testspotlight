import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ScheduleRoutingModule } from './schedule-routing.module';
import { ScheduleComponent } from './schedule.component';
import { ScheduleService } from './schedule.service';
import { ScheduleListComponent } from './schedule-list/schedule-list.component';
import { ScheduleDtlComponent } from './schedule-dtl/schedule-dtl.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';
import { ScheduleDtlDailyComponent } from './schedule-dtl/schedule-dtl-daily/schedule-dtl-daily.component';
import { ScheduleDtlWeeklyComponent } from './schedule-dtl/schedule-dtl-weekly/schedule-dtl-weekly.component';
import { ScheduleDtlMonthlyComponent } from './schedule-dtl/schedule-dtl-monthly/schedule-dtl-monthly.component';
import { ScheduleDtlCustomComponent } from './schedule-dtl/schedule-dtl-custom/schedule-dtl-custom.component';
import { ScheduleDtlSubmissionsComponent } from './schedule-dtl/schedule-dtl-submissions/schedule-dtl-submissions.component';

@NgModule({
  declarations: [
    ScheduleComponent,
    ScheduleListComponent,
    ScheduleDtlComponent,
    ScheduleDtlDailyComponent,
    ScheduleDtlWeeklyComponent,
    ScheduleDtlMonthlyComponent,
    ScheduleDtlCustomComponent,
    ScheduleDtlSubmissionsComponent
  ],
  imports: [CommonModule, ScheduleRoutingModule, SharedModule, PrimengModule, FormsModule, ReactiveFormsModule],
  providers: [ScheduleService],
  exports: [ScheduleDtlMonthlyComponent]
})
export class ScheduleModule {}
