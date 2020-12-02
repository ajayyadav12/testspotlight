import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubmissionsStepsComponent } from './submissions-steps.component';
import { GeMaterialModule } from 'src/app/shared/ge-material/ge-material.module';

@NgModule({
  declarations: [SubmissionsStepsComponent],
  imports: [CommonModule, GeMaterialModule]
})
export class SubmissionsStepsModule {}
