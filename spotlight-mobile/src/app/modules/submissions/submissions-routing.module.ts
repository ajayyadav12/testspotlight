import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SubmissionsComponent } from './submissions.component';
import { SubmissionsStepsComponent } from './submissions-steps/submissions-steps.component';

const routes: Routes = [
  {
    path: '',
    component: SubmissionsComponent
  },
  {
    path: 'steps/:id',
    component: SubmissionsStepsComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SubmissionsRoutingModule {}
