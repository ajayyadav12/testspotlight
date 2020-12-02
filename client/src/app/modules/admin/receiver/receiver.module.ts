import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ReceiverRoutingModule } from './receiver-routing.module';
import { ReceiverComponent } from './receiver.component';
import { ReactiveFormsModule } from '@angular/forms';
import { ReceiverDtlComponent } from './receiver-dtl/receiver-dtl.component';
import { ReceiverListComponent } from './receiver-list/receiver-list.component';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [ReceiverComponent, ReceiverDtlComponent, ReceiverListComponent],
  imports: [CommonModule, ReceiverRoutingModule, SharedModule, ReactiveFormsModule],  
})
export class ReceiverModule {}
