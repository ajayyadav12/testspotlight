import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SystemRoutingModule } from './system-routing.module';
import { SystemComponent } from './system.component';

import { SystemListComponent } from './system-list/system-list.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { SystemDtlModule } from './system-dtl/system-dtl.module';

@NgModule({
  declarations: [SystemComponent, SystemListComponent],
  imports: [CommonModule, SystemRoutingModule, SharedModule, SystemDtlModule]
})
export class SystemModule { }
