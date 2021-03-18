import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StatusRoutingModule } from './status-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { StatusComponent } from './status.component';
import { ScheduleReportListComponent } from './schedule-report/schedule-report-list/schedule-report-list.component';
import { ScheduleReportComponent } from './schedule-report/schedule-report.component';
import { StatusChildrenComponent } from './level-parent/status-children/status-children.component';
import { StatusParentComponent } from './level-parent/status-parent/status-parent.component';
import { StatusSubmissionComponent } from './level-child/status-submission/status-submission.component';
import { DataDistributionComponent } from './level-child/data-distribution/data-distribution.component';
import { StatusStepsComponent } from './level-child/status-steps/status-steps.component';
import { ScheduleDtlMonthlyModule } from 'src/app/modules/admin/schedule/schedule-dtl/schedule-dtl-monthly/schedule-dtl-monthly.module';
import { PeriodAndBuModule } from '../period-and-bu/period-and-bu/period-and-bu.module';



@NgModule({
  declarations: [
    StatusComponent,
    StatusSubmissionComponent,
    DataDistributionComponent,
    StatusStepsComponent,
    ScheduleReportComponent,
    StatusParentComponent,
    StatusChildrenComponent,
    ScheduleReportListComponent
  ],
  imports: [
    CommonModule,
    StatusRoutingModule,
    SharedModule,
    ScheduleDtlMonthlyModule,
    PeriodAndBuModule
  ]
})
export class StatusModule { }
