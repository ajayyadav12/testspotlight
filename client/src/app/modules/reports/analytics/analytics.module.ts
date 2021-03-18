import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SharedModule } from 'src/app/shared/shared.module';

import { AnalyticsRoutingModule } from './analytics-routing.module';
import { AnalyticsComponent } from './analytics.component';
import { AnalyticsService } from './analytics.service';

import { ProcessModule } from 'src/app/modules/admin/process/process.module';
import { SubmissionsService } from '../submissions/submissions.service';
import { NotificationService } from '../../admin/notification/notification.service';
import { TrendComponent } from './trend/trend.component';
import { TrendChartComponent } from './trend/trend-chart/trend-chart.component';
import { TrendExportComponent } from './trend/trend-export/trend-export.component';
import { PeriodAndBuComponent } from './period-and-bu/period-and-bu.component';
import { PeriodAndBuModule } from './period-and-bu/period-and-bu/period-and-bu.module';
import { SubmissionsStepsModule } from '../submissions/submissions-steps/submissions-steps.module';

@NgModule({
	declarations: [AnalyticsComponent, TrendComponent, TrendChartComponent, TrendExportComponent],
	imports: [
		CommonModule,
		AnalyticsRoutingModule,
		SharedModule,
		ProcessModule,
		PeriodAndBuModule,
		SubmissionsStepsModule,
	],
	providers: [AnalyticsService, SubmissionsService, NotificationService],
})
export class AnalyticsModule {}
