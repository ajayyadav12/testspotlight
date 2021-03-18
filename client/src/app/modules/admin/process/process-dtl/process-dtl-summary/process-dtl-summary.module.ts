import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProcessDtlSummaryComponent } from './process-dtl-summary.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [ProcessDtlSummaryComponent],
  imports: [CommonModule, SharedModule],
  exports: [ProcessDtlSummaryComponent],
})
export class ProcessDtlSummaryModule {}
