import { ProcessDtlApproveComponent } from './process-dtl/process-dtl-approve/process-dtl-approve.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ProcessListComponent } from './process-list/process-list.component';
import { ProcessComponent } from './process.component';

const routes: Routes = [
  {
    path: '',
    component: ProcessComponent,
    children: [
      { path: '', component: ProcessListComponent },
      { path: ':id', loadChildren: () => import('./process-dtl/process-dtl.module').then(m => m.ProcessDtlModule) },
      { path: ':id/approve', component: ProcessDtlApproveComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProcessRoutingModule { }
