import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GeUpdateScheduledSubmissionComponent } from './ge-update-scheduled-submission.component';
import { PrimengModule } from '../../primeng.module';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [GeUpdateScheduledSubmissionComponent],
  imports: [CommonModule, PrimengModule, ReactiveFormsModule],
  exports: [GeUpdateScheduledSubmissionComponent]
})
export class GeUpdateScheduledSubmissionModule {}
