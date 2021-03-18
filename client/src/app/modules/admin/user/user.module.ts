import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserRoutingModule } from './user-routing.module';
import { UserComponent } from './user.component';
import { UserListComponent } from './user-list/user-list.component';
import { UserService } from './user.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { UserDtlModule } from './user-dtl/user-dtl.module';

@NgModule({
  declarations: [UserComponent, UserListComponent],
  imports: [CommonModule, UserRoutingModule, SharedModule, UserDtlModule],
  providers: [UserService]
})
export class UserModule {}
