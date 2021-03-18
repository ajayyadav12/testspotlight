import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SystemsLoadComponent } from './systems-load.component';


const routes: Routes = [
  {
    path: '',
    component: SystemsLoadComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SystemsLoadRoutingModule { }
