import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RelationshipComponent } from './relationship.component';
import { RelationshipRoutingModule } from './relationship-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { RelationshipService } from './relationship.service';
import { RelationshipProcessesModule } from './relationship-processes/relationship-processes.module';
import { RelationshipClosePhasesModule } from './relationship-close-phases/relationship-close-phases.module';

@NgModule({
	declarations: [ RelationshipComponent ],
	imports: [
		CommonModule,
		RelationshipRoutingModule,
		RelationshipProcessesModule,
		RelationshipClosePhasesModule,
		SharedModule
	],
	providers: [ RelationshipService ]
})
export class RelationshipModule {}
