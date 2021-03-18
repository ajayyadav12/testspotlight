import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProcessDtlCriticalComponent } from './process-dtl-critical/process-dtl-critical.component';
import { ProcessDtlRoutingModule } from './process-dtl-routing.module';

import { SharedModule } from 'src/app/shared/shared.module';
import { ProcessDtlCriticalCalendarListComponent } from './process-dtl-critical/process-dtl-critical-calendar/process-dtl-critical-calendar-list/process-dtl-critical-calendar-list/process-dtl-critical-cal-list.component';
import { ProcessDtlCriticalCalendarDtlComponent } from './process-dtl-critical/process-dtl-critical-calendar/process-dtl-critical-calendar-dtl/process-dtl-critical-calendar-dtl/process-dtl-critical-cal-dtl.component';
import { ProcessDtlComponent } from './process-dtl.component';
import { ProcessDtlStepsComponent } from './process-dtl-steps/process-dtl-steps.component';
import { ProcessDtlUsersComponent } from './process-dtl-users/process-dtl-users.component';
import { ProcessDtlNotificationsComponent } from './process-dtl-notifications/process-dtl-notifications.component';
import { ProcessDtlTreeComponent } from './process-dtl-tree/process-dtl-tree.component';
import { ScheduleDtlWeeklyModule } from '../../schedule/schedule-dtl/schedule-dtl-weekly/schedule-dtl-weekly.module';
import { ScheduleDtlMonthlyModule } from '../../schedule/schedule-dtl/schedule-dtl-monthly/schedule-dtl-monthly.module';
import { ScheduleDtlCustomModule } from '../../schedule/schedule-dtl/schedule-dtl-custom/schedule-dtl-custom.module';
import { ProcessDtlExportComponent } from './process-dtl-export/process-dtl-export.component';
import { ProcessDtlNotificationsAlertsModule } from './process-dtl-notifications/process-dtl-notifications-alerts/process-dtl-notifications-alerts.module';
import { ProcessDtlSummaryModule } from './process-dtl-summary/process-dtl-summary.module';

@NgModule({
  declarations: [
    ProcessDtlCriticalCalendarListComponent,
    ProcessDtlCriticalCalendarDtlComponent,
    ProcessDtlCriticalComponent,
    ProcessDtlComponent,
    ProcessDtlStepsComponent,
    ProcessDtlUsersComponent,
    ProcessDtlNotificationsComponent,
    ProcessDtlTreeComponent,
    ProcessDtlExportComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    ScheduleDtlWeeklyModule,
    ScheduleDtlMonthlyModule,
    ScheduleDtlCustomModule,
    ProcessDtlRoutingModule,
    ProcessDtlNotificationsAlertsModule,
    ProcessDtlSummaryModule,
  ],
  exports: [ProcessDtlCriticalComponent],
})
export class ProcessDtlModule {}
