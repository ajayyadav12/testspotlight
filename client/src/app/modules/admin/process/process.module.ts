import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';

import { ProcessRoutingModule } from './process-routing.module';
import { ProcessComponent } from './process.component';
import { ProcessService } from './process.service';
import { ProcessListComponent } from './process-list/process-list.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ProcessDtlApproveComponent } from './process-dtl/process-dtl-approve/process-dtl-approve.component';
import { ProcessDtlSummaryModule } from './process-dtl/process-dtl-summary/process-dtl-summary.module';

@NgModule({
  declarations: [
    ProcessComponent,
    ProcessListComponent,
    ProcessDtlApproveComponent,
  ],
  imports: [
    CommonModule,
    ProcessRoutingModule,
    SharedModule,
    ProcessDtlSummaryModule,
  ],
  providers: [ProcessService],
  exports: [],
})
export class ProcessModule {}
