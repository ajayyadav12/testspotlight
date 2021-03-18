import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RelationshipProcessesComponent } from './relationship-processes.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { SubmissionsStepsModule } from '../../reports/submissions/submissions-steps/submissions-steps.module';



@NgModule({
  declarations: [RelationshipProcessesComponent],
  imports: [
    CommonModule,
    SharedModule,
    SubmissionsStepsModule
  ],
  exports: [RelationshipProcessesComponent]
})
export class RelationshipProcessesModule { }
