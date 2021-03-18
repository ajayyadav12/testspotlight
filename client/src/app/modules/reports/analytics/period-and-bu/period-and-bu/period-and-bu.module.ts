import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { PeriodAndBuComponent } from '../period-and-bu.component';



@NgModule({
  declarations: [PeriodAndBuComponent],
  exports: [PeriodAndBuComponent],
  imports: [
    CommonModule,
    SharedModule
  ]
})
export class PeriodAndBuModule { }
