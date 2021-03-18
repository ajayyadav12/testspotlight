import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardOpsComponent } from './../ops/dashboard-ops.component';
import { DashboardOpsRoutingModule } from './../ops/dashboard-ops-routing.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { DashboardOpsFiltersModule } from './ops-filters/dashboard-ops-filters.module';
import { FormsModule } from '@angular/forms';
import { OpsCurrentModule } from './ops-current/ops-current.module';
import { OpsDetailsModule } from './ops-details/ops-details.module';
import { OpsUpcomingComponent } from './ops-upcoming/ops-upcoming.component';

@NgModule({
  declarations: [DashboardOpsComponent, OpsUpcomingComponent],
  imports: [
    CommonModule,
    FormsModule,
    DashboardOpsRoutingModule,
    PrimengModule,
    SharedModule,
    OpsCurrentModule,
    OpsDetailsModule,
    DashboardOpsFiltersModule
  ],
  exports: [DashboardOpsComponent] 
})
export class DashboardOpsModule { }
