import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { SystemDtlComponent } from './system-dtl.component';



@NgModule({
  declarations: [SystemDtlComponent],
  imports: [
    CommonModule,
    SharedModule,    
  ],
  exports: [SystemDtlComponent]
})
export class SystemDtlModule { }
