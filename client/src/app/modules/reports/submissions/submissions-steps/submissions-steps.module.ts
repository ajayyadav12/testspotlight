import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from 'src/app/shared/shared.module';
import { SubmissionsStepsComponent } from './submissions-steps.component';



@NgModule({
  declarations: [SubmissionsStepsComponent],
  imports: [
    CommonModule, SharedModule
  ],
  exports: [SubmissionsStepsComponent]
})
export class SubmissionsStepsModule { }
