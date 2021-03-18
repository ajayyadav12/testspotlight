import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SubmissionsRoutingModule } from './submissions-routing.module';
import { SubmissionsComponent } from './submissions.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SubmissionsChildrenComponent } from './submissions-children/submissions-children.component';
import { SubmissionsParentsComponent } from './submissions-parents/submissions-parents.component';
import { SubmissionsIsolationComponent } from './submissions-isolation/submissions-isolation.component';
import { ProcessService } from '../../admin/process/process.service';
import { SharedModule } from 'src/app/shared/shared.module';
import { SubmissionsCalendarComponent } from './submissions-calendar/submissions-calendar.component';
import { SubmissionsService } from './submissions.service';
import { SubmissionsManualClosingModule } from './submissions-manual-closing/submissions-manual-closing.module';
import { SubmissionsStepsModule } from './submissions-steps/submissions-steps.module';

@NgModule({
	declarations: [
		SubmissionsComponent,
		SubmissionsChildrenComponent,
		SubmissionsParentsComponent,
		SubmissionsIsolationComponent,
		SubmissionsCalendarComponent
	],
	imports: [
		CommonModule,
		SubmissionsStepsModule,
		SubmissionsRoutingModule,
		SharedModule,
		FormsModule,
		ReactiveFormsModule,
		SubmissionsManualClosingModule
	],
	exports: [ SubmissionsComponent, SubmissionsCalendarComponent ],
	providers: [ SubmissionsService, ProcessService ]
})
export class SubmissionsModule {}
