import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardTrendingChartComponent } from './dashboard-trending-chart.component';
import { PrimengModule } from 'src/app/shared/primeng.module';

@NgModule({
  declarations: [DashboardTrendingChartComponent],
  imports: [CommonModule, PrimengModule],
  exports: [DashboardTrendingChartComponent]
})
export class DashboardTrendingChartModule {}
