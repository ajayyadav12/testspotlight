import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { UserComponent } from './user.component';
import { UserListComponent } from './user-list/user-list.component';
import { UserDtlComponent } from './user-dtl/user-dtl.component';

const routes: Routes = [
  {
    path: '',
    component: UserComponent,
    children: [{ path: '', component: UserListComponent }, { path: ':id', component: UserDtlComponent }]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule {}
