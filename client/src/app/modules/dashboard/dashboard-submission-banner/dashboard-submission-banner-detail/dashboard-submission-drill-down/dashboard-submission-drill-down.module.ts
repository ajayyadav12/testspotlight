import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardSubmissionDrillDownComponent } from './dashboard-submission-drill-down.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SubmissionsService } from '../../../../reports/submissions/submissions.service';
import { ProcessService } from '../../../../admin/process/process.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { SubmissionsManualClosingModule } from 'src/app/modules/reports/submissions/submissions-manual-closing/submissions-manual-closing.module';

@NgModule({
  declarations: [
    DashboardSubmissionDrillDownComponent
  ],
  imports: [CommonModule, SharedModule, FormsModule, ReactiveFormsModule, SubmissionsManualClosingModule],
  exports: [DashboardSubmissionDrillDownComponent],
  providers: [SubmissionsService, ProcessService]
})
export class DashboardSubmissionDrillDownModule { }
