import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { ScheduleDtlMonthlyComponent } from './schedule-dtl-monthly.component';



@NgModule({
  declarations: [ScheduleDtlMonthlyComponent],
  imports: [
    CommonModule, SharedModule
  ],
  exports: [ScheduleDtlMonthlyComponent]
})
export class ScheduleDtlMonthlyModule { }
