import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardOpsComponent } from './dashboard-ops.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardOpsComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardOpsRoutingModule {}
