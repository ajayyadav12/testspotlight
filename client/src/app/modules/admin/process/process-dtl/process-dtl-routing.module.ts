import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProcessDtlCriticalComponent } from './process-dtl-critical/process-dtl-critical.component';
import { ProcessDtlCriticalCalendarDtlComponent } from './process-dtl-critical/process-dtl-critical-calendar/process-dtl-critical-calendar-dtl/process-dtl-critical-calendar-dtl/process-dtl-critical-cal-dtl.component';
import { ProcessDtlComponent } from './process-dtl.component';
import { ProcessDtlSummaryComponent } from './process-dtl-summary/process-dtl-summary.component';
import { ProcessDtlStepsComponent } from './process-dtl-steps/process-dtl-steps.component';
import { ProcessDtlNotificationsComponent } from './process-dtl-notifications/process-dtl-notifications.component';
import { ProcessDtlUsersComponent } from './process-dtl-users/process-dtl-users.component';
import { ProcessDtlTreeComponent } from './process-dtl-tree/process-dtl-tree.component';
import { ProcessDtlExportComponent } from './process-dtl-export/process-dtl-export.component';
import { ProcessDtlCriticalCalendarListComponent } from './process-dtl-critical/process-dtl-critical-calendar/process-dtl-critical-calendar-list/process-dtl-critical-calendar-list/process-dtl-critical-cal-list.component';

const routes: Routes = [
  {

    path: '',
    component: ProcessDtlComponent,
    children: [
      { path: '', redirectTo: 'summary', pathMatch: 'full' },
      { path: 'summary', component: ProcessDtlSummaryComponent },
      { path: 'steps', component: ProcessDtlStepsComponent },
      { path: 'notifications', component: ProcessDtlNotificationsComponent },
      { path: 'users', component: ProcessDtlUsersComponent },
      { path: 'parent-setup', component: ProcessDtlTreeComponent },
      { path: 'critical', component: ProcessDtlCriticalCalendarListComponent },
      { path: 'critical/:schedid', component: ProcessDtlCriticalCalendarDtlComponent},
      { path: 'export', component: ProcessDtlExportComponent },
    ]
  }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProcessDtlRoutingModule { }
