import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SystemComponent } from './system.component';
import { SystemListComponent } from './system-list/system-list.component';
import { SystemDtlComponent } from './system-dtl/system-dtl.component';

const routes: Routes = [
  {
    path: '',
    component: SystemComponent,
    children: [
      { path: '', component: SystemListComponent },
      { path: ':id', component: SystemDtlComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SystemRoutingModule { }
