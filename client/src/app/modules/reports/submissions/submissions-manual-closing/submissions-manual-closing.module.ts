import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubmissionsManualClosingComponent } from './submissions-manual-closing.component';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';



@NgModule({
  declarations: [SubmissionsManualClosingComponent],
  imports: [
    CommonModule, ReactiveFormsModule, SharedModule
  ],
  exports: [SubmissionsManualClosingComponent]
})
export class SubmissionsManualClosingModule { }
