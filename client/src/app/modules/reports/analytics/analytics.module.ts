import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from 'src/app/shared/shared.module';

import { AnalyticsRoutingModule } from './analytics-routing.module';
import { AnalyticsComponent } from './analytics.component';
import { AnalyticsService } from './analytics.service';

import { VarianceComponent } from './variance/variance.component';
import { VarianceChartComponent } from './variance/variance-chart/variance-chart.component';
import { VarianceExportComponent } from './variance/variance-export/variance-export.component';

import { StatusComponent } from './status/status.component';
import { StatusSubmissionComponent } from './status/level-child/status-submission/status-submission.component';
import { DataDistributionComponent } from './status/level-child/data-distribution/data-distribution.component';
import { StatusStepsComponent } from './status/level-child/status-steps/status-steps.component';

import { ScheduleReportComponent } from './status/schedule-report/schedule-report.component';

import { ProcessModule } from 'src/app/modules/admin/process/process.module';
import { SubmissionsService } from '../submissions/submissions.service';
import { ScheduleModule } from '../../admin/schedule/schedule.module';
import { NotificationService } from '../../admin/notification/notification.service';
import { StatusParentComponent } from './status/level-parent/status-parent/status-parent.component';
import { StatusChildrenComponent } from './status/level-parent/status-children/status-children.component';
import { ScheduleReportListComponent } from './status/schedule-report/schedule-report-list/schedule-report-list.component';
import { TrendComponent } from './trend/trend.component';
import { TrendChartComponent } from './trend/trend-chart/trend-chart.component';
import { TrendExportComponent } from './trend/trend-export/trend-export.component';

@NgModule({
  declarations: [
    AnalyticsComponent,
    VarianceComponent,
    VarianceChartComponent,
    VarianceExportComponent,
    StatusComponent,
    StatusSubmissionComponent,
    DataDistributionComponent,
    StatusStepsComponent,
    ScheduleReportComponent,
    StatusParentComponent,
    StatusChildrenComponent,
    ScheduleReportListComponent,
    TrendComponent,
    TrendChartComponent,
    TrendExportComponent
  ],
  imports: [
    CommonModule,
    AnalyticsRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    ProcessModule,
    ScheduleModule
  ],
  providers: [AnalyticsService, SubmissionsService, NotificationService, DatePipe]
})
export class AnalyticsModule {}
