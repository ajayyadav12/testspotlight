import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScheduleDtlCustomComponent } from './schedule-dtl-custom.component';
import { SharedModule } from 'src/app/shared/shared.module';




@NgModule({
  declarations: [ScheduleDtlCustomComponent],
  imports: [
    CommonModule, SharedModule
  ],
  exports: [ScheduleDtlCustomComponent]
})
export class ScheduleDtlCustomModule { }
