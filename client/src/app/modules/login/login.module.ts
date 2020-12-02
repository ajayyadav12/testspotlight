import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { LoginRoutingModule } from './login-routing.module';
import { LoginComponent } from './login.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { ReactiveFormsModule } from '@angular/forms';
import { AccountComponent } from './account/account.component';

@NgModule({
  declarations: [LoginComponent, AccountComponent],
  imports: [CommonModule, LoginRoutingModule, PrimengModule, ReactiveFormsModule]
})
export class LoginModule {}
