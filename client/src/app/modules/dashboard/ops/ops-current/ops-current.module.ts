import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { DashboardOpsRoutingModule } from '../dashboard-ops-routing.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SelectButtonModule } from 'primeng/selectbutton';
import { OpsCurrentComponent } from './ops-current.component';


@NgModule({
  declarations: [OpsCurrentComponent],
  imports: [CommonModule, DashboardOpsRoutingModule, FormsModule, SharedModule, ReactiveFormsModule, SelectButtonModule],
  exports: [OpsCurrentComponent]
})
export class OpsCurrentModule { }