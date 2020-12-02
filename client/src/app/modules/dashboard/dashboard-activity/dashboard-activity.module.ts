import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardActivityComponent } from './dashboard-activity.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { DashboardRoutingModule } from '../dashboard-routing.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SubmissionsModule } from '../../reports/submissions/submissions.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SelectButtonModule } from 'primeng/selectbutton';


@NgModule({
  declarations: [DashboardActivityComponent],
  imports: [CommonModule, DashboardRoutingModule, FormsModule, SubmissionsModule, SharedModule, ReactiveFormsModule, SelectButtonModule],
  exports: [DashboardActivityComponent]
})
export class DashboardActivityModule { }
