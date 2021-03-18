import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RelationshipClosePhasesComponent } from './relationship-close-phases.component';
import { SharedModule } from 'src/app/shared/shared.module';



@NgModule({
  declarations: [RelationshipClosePhasesComponent],
  imports: [
    CommonModule,
    SharedModule,
  ]
})
export class RelationshipClosePhasesModule { }
