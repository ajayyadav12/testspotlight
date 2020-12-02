import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ReceiverComponent } from './receiver.component';
import { ReceiverListComponent } from './receiver-list/receiver-list.component';
import { ReceiverDtlComponent } from './receiver-dtl/receiver-dtl.component';

const routes: Routes = [
  {
    path: '',
    component: ReceiverComponent,
    children: [
      { path: '', component: ReceiverListComponent },
      { path: ':id', component: ReceiverDtlComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReceiverRoutingModule {}
