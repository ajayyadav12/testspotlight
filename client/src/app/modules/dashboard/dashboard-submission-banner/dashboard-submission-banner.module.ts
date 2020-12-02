import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoogleCharts } from 'google-charts';
import { DashboardSubmissionBannerComponent } from './dashboard-submission-banner.component';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [DashboardSubmissionBannerComponent],
  imports: [CommonModule, FormsModule],
  exports: [DashboardSubmissionBannerComponent]
})
export class DashboardSubmissionBannerModule { }
