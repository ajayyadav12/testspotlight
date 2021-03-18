import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RelationshipComponent } from './relationship.component';
import { RelationshipProcessesComponent } from './relationship-processes/relationship-processes.component';
import { RelationshipClosePhasesComponent } from './relationship-close-phases/relationship-close-phases.component';

const routes: Routes = [
  {
    path: '',
    component: RelationshipComponent,
    children: [
      { path: '', component: RelationshipClosePhasesComponent },
      {
        path: ':id',
        component: RelationshipProcessesComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RelationshipRoutingModule { }
