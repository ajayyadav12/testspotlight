import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { VarianceRoutingModule } from './variance-routing.module';
import { VarianceComponent } from './variance.component';
import { VarianceChartComponent } from './variance-chart/variance-chart.component';
import { VarianceExportComponent } from './variance-export/variance-export.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { PeriodAndBuModule } from '../period-and-bu/period-and-bu/period-and-bu.module';


@NgModule({
  declarations: [
    VarianceComponent,
    VarianceChartComponent,
    VarianceExportComponent,
  ],
  imports: [
    CommonModule,
    VarianceRoutingModule, SharedModule, PeriodAndBuModule
  ]
})
export class VarianceModule { }
