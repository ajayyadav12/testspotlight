import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { GENotesDialogComponent } from './Components/ge-notes-dialog.component';
import { AutofocusDirective } from './Directives/autofocus.directive';
import { NgModule } from '@angular/core';
import { PrimengModule } from './primeng.module';
import { GeTableComponent } from './ge-table/ge-table.component';
import { CommonModule } from '@angular/common';
import { GEViewsComponent } from './Components/ge-views/ge-views.component';
import { GEFiltersComponent } from './Components/ge-filters/ge-filters.component';

@NgModule({
  declarations: [GeTableComponent, AutofocusDirective, GENotesDialogComponent, GEViewsComponent, GEFiltersComponent],
  imports: [CommonModule, PrimengModule, FormsModule, ReactiveFormsModule],
  exports: [
    PrimengModule,
    GeTableComponent,
    AutofocusDirective,
    GENotesDialogComponent,
    GEViewsComponent,
    GEFiltersComponent
  ]
})
export class SharedModule {}
