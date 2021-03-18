import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SystemsLoadRoutingModule } from './systems-load-routing.module';
import { SystemsLoadComponent } from './systems-load.component';


@NgModule({
  declarations: [SystemsLoadComponent],
  imports: [
    CommonModule,
    SystemsLoadRoutingModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class SystemsLoadModule { }
