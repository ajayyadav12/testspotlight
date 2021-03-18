import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SlaRoutingModule } from './sla-routing.module';
import { SlaComponent } from './sla.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { SlaW2wDetailsModule } from './sla-w2w-details/sla-w2w-details.module';
import { SlaTrendModule } from './sla-trend/sla-trend.module';
import { SlaFiltersComponent } from './sla-filters/sla-filters.component';

@NgModule({
	declarations: [SlaComponent, SlaFiltersComponent],
	imports: [CommonModule, SlaRoutingModule, SharedModule, SlaW2wDetailsModule, SlaTrendModule]
})
export class SlaModule {}
