import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { GENotesDialogComponent } from './Components/ge-notes-dialog.component';
import { AutofocusDirective } from './Directives/autofocus.directive';
import { NgModule } from '@angular/core';
import { PrimengModule } from './primeng.module';
import { GeTableComponent } from './ge-table/ge-table.component';
import { CommonModule } from '@angular/common';
import { GEViewsComponent } from './Components/ge-views/ge-views.component';
import { GEFiltersComponent } from './Components/ge-filters/ge-filters.component';
import { GeUpdateScheduledSubmissionModule } from './Components/ge-update-scheduled-submission/ge-update-scheduled-submission.module';
import { GeProcessCopyComponent } from './Components/ge-process-copy/ge-process-copy.component';
import { GeStatusLegendModule } from './Components/ge-status-legend/ge-status-legend.module';

@NgModule({
	declarations: [
		GeTableComponent,
		AutofocusDirective,
		GENotesDialogComponent,
		GEViewsComponent,
		GEFiltersComponent,
		GeProcessCopyComponent
	],
	imports: [CommonModule, PrimengModule, FormsModule, ReactiveFormsModule, GeStatusLegendModule],
	exports: [
		PrimengModule,
		GeTableComponent,
		AutofocusDirective,
		GENotesDialogComponent,
		GEViewsComponent,
		GEFiltersComponent,
		GeUpdateScheduledSubmissionModule,
		GeProcessCopyComponent,
		FormsModule,
		ReactiveFormsModule,
		GeStatusLegendModule
	]
})
export class SharedModule {}
