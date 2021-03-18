import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserDtlPermissionComponent } from './user-dtl-permission/user-dtl-permission.component';
import { UserDtlComponent } from './user-dtl.component';
import { SharedModule } from 'src/app/shared/shared.module';



@NgModule({
  declarations: [UserDtlComponent, UserDtlPermissionComponent],
  imports: [
    CommonModule, SharedModule
  ], 
  exports: [UserDtlComponent]
})
export class UserDtlModule { }
