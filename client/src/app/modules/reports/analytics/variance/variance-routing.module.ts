import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { VarianceComponent } from './variance.component';


const routes: Routes = [
  {
    path: '',
    component: VarianceComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VarianceRoutingModule { }
