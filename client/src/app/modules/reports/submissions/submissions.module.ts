import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';

import { SubmissionsRoutingModule } from './submissions-routing.module';
import { SubmissionsComponent } from './submissions.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SubmissionsStepsComponent } from './submissions-steps/submissions-steps.component';
import { SubmissionsChildrenComponent } from './submissions-children/submissions-children.component';
import { SubmissionsParentsComponent } from './submissions-parents/submissions-parents.component';
import { SubmissionsIsolationComponent } from './submissions-isolation/submissions-isolation.component';
import { ProcessService } from '../../admin/process/process.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { SubmissionsManualClosingComponent } from './submissions-manual-closing/submissions-manual-closing.component';
import { SubmissionsCalendarComponent } from './submissions-calendar/submissions-calendar.component';
import { SubmissionsService } from './submissions.service';

@NgModule({
  declarations: [
    SubmissionsComponent,
    SubmissionsStepsComponent,
    SubmissionsChildrenComponent,
    SubmissionsParentsComponent,
    SubmissionsIsolationComponent,
    SubmissionsManualClosingComponent,
    SubmissionsCalendarComponent
  ],
  imports: [CommonModule, SubmissionsRoutingModule, SharedModule, FormsModule, ReactiveFormsModule],
  exports: [SubmissionsComponent, SubmissionsCalendarComponent],
  providers: [SubmissionsService, ProcessService, DatePipe]
})
export class SubmissionsModule {}
