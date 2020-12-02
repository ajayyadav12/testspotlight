import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './dashboard.component';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { DashboardSubmissionBannerModule } from './dashboard-submission-banner/dashboard-submission-banner.module';
import { DashboardTrendingChartModule } from './dashboard-trending-chart/dashboard-trending-chart.module';
import { DashboardSubmissionDrillDownModule } from './dashboard-submission-banner/dashboard-submission-banner-detail/dashboard-submission-drill-down/dashboard-submission-drill-down.module';
import { DashboardActivityModule } from './dashboard-activity/dashboard-activity.module';
import { DashboardFiltersModule } from './dashboard-filters/dashboard-filters.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [DashboardComponent],
  imports: [
    CommonModule,
    FormsModule,
    DashboardRoutingModule,
    PrimengModule,
    SharedModule,
    DashboardSubmissionBannerModule,
    DashboardSubmissionDrillDownModule,
    DashboardTrendingChartModule,
    DashboardActivityModule,
    DashboardFiltersModule
  ]
})
export class DashboardModule { }
