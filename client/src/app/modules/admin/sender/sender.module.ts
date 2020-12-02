import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SenderRoutingModule } from './sender-routing.module';
import { SenderComponent } from './sender.component';

import { ReactiveFormsModule } from '@angular/forms';
import { SenderDtlComponent } from './sender-dtl/sender-dtl.component';
import { SenderListComponent } from './sender-list/sender-list.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [SenderComponent, SenderDtlComponent, SenderListComponent],
  imports: [CommonModule, SenderRoutingModule, SharedModule, ReactiveFormsModule]
})
export class SenderModule {}
