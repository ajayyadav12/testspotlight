import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { UserRoutingModule } from './user-routing.module';
import { UserComponent } from './user.component';
import { UserListComponent } from './user-list/user-list.component';
import { UserDtlComponent } from './user-dtl/user-dtl.component';
import { UserService } from './user.service';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [UserComponent, UserListComponent, UserDtlComponent],
  imports: [CommonModule, UserRoutingModule, SharedModule, ReactiveFormsModule],
  providers: [UserService]
})
export class UserModule {}
