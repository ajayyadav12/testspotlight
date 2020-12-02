import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SenderComponent } from './sender.component';
import { SenderListComponent } from './sender-list/sender-list.component';
import { SenderDtlComponent } from './sender-dtl/sender-dtl.component';

const routes: Routes = [
  {
    path: '',
    component: SenderComponent,
    children: [
      { path: '', component: SenderListComponent },
      { path: ':id', component: SenderDtlComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SenderRoutingModule {}
