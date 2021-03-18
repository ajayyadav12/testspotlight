import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ScheduleRoutingModule } from './schedule-routing.module';
import { ScheduleComponent } from './schedule.component';
import { ScheduleService } from './schedule.service';
import { ScheduleListComponent } from './schedule-list/schedule-list.component';
import { ScheduleDtlComponent } from './schedule-dtl/schedule-dtl.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { ScheduleDtlSubmissionsComponent } from './schedule-dtl/schedule-dtl-submissions/schedule-dtl-submissions.component';
import { ScheduleDtlFormModule } from './schedule-dtl/schedule-dtl-form/schedule-dtl-form.module';

@NgModule({
  declarations: [
    ScheduleComponent,
    ScheduleListComponent,
    ScheduleDtlComponent,    
    ScheduleDtlSubmissionsComponent
  ],
  imports: [CommonModule,
    ScheduleRoutingModule,
    SharedModule,    
    ScheduleDtlFormModule
    ],
  providers: [ScheduleService],
  exports: []
})
export class ScheduleModule { }
