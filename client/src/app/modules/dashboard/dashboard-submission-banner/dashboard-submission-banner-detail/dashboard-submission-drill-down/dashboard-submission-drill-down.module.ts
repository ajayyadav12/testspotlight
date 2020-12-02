import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';

import { SubmissionsRoutingModule } from '../../../../reports/submissions/submissions-routing.module';
import { DashboardSubmissionDrillDownComponent } from './dashboard-submission-drill-down.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
//import { SubmissionsStepsComponent } from '../../../../reports/submissions/submissions-steps/submissions-steps.component';
//import { SubmissionsFilterComponent } from '../../../dashboard-filters/dashboard-filters.component';
import { SubmissionsService } from '../../../../reports/submissions/submissions.service';
import { SubmissionsChildrenComponent } from '../../../../reports/submissions/submissions-children/submissions-children.component';
import { SubmissionsParentsComponent } from '../../../../reports/submissions/submissions-parents/submissions-parents.component';
//import { SubmissionsViewsComponent } from '../../../../reports/submissions/submissions-views/submissions-views.component';
import { SubmissionsIsolationComponent } from '../../../../reports/submissions/submissions-isolation/submissions-isolation.component';
import { ProcessService } from '../../../../admin/process/process.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { SubmissionsManualClosingComponent } from '../../../../reports/submissions/submissions-manual-closing/submissions-manual-closing.component';
import { SubmissionsCalendarComponent } from '../../../../reports/submissions/submissions-calendar/submissions-calendar.component';
import { SubmissionsComponent } from '../../../../reports/submissions/submissions.component';

@NgModule({
  declarations: [
    DashboardSubmissionDrillDownComponent
    //SubmissionsComponent,
    //SubmissionsStepsComponent,
    //SubmissionsFilterComponent,
    //SubmissionsChildrenComponent,
    //SubmissionsParentsComponent,
    //SubmissionsViewsComponent,
    //SubmissionsIsolationComponent,
    //SubmissionsManualClosingComponent,
    //SubmissionsCalendarComponent
  ],
  imports: [CommonModule, SharedModule, FormsModule, ReactiveFormsModule],
  exports: [DashboardSubmissionDrillDownComponent],
  providers: [SubmissionsService, ProcessService, DatePipe]
})
export class DashboardSubmissionDrillDownModule {}
