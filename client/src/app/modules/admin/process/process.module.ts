import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { ProcessRoutingModule } from './process-routing.module';
import { ProcessComponent } from './process.component';
import { ProcessService } from './process.service';
import { ProcessListComponent } from './process-list/process-list.component';
import { ProcessDtlComponent } from './process-dtl/process-dtl.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ProcessDtlStepsComponent } from './process-dtl/process-dtl-steps/process-dtl-steps.component';
import { ProcessDtlUsersComponent } from './process-dtl/process-dtl-users/process-dtl-users.component';
import { ProcessDtlNotificationsComponent } from './process-dtl/process-dtl-notifications/process-dtl-notifications.component';
import { ProcessDtlChartComponent } from './process-dtl/process-dtl-chart/process-dtl-chart.component';
import { ProcessDtlApproveComponent } from './process-dtl/process-dtl-approve/process-dtl-approve.component';
import { ProcessDtlSummaryComponent } from './process-dtl/process-dtl-summary/process-dtl-summary.component';
import { ProcessDtlTreeComponent } from './process-dtl/process-dtl-tree/process-dtl-tree.component';

@NgModule({
  declarations: [
    ProcessComponent,
    ProcessListComponent,
    ProcessDtlComponent,
    ProcessDtlStepsComponent,
    ProcessDtlUsersComponent,
    ProcessDtlNotificationsComponent,
    ProcessDtlChartComponent,
    ProcessDtlApproveComponent,
    ProcessDtlSummaryComponent,
    ProcessDtlTreeComponent
  ],
  imports: [CommonModule, ProcessRoutingModule, SharedModule, ReactiveFormsModule, FormsModule],
  providers: [ProcessService],
  exports: [ProcessDtlChartComponent]
})
export class ProcessModule {}
