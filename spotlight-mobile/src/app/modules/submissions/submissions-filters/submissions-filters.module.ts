import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubmissionsFiltersComponent } from './submissions-filters.component';
import { SubmissionsFiltersService } from './submissions-filters.service';
import { GeMaterialModule } from 'src/app/shared/ge-material/ge-material.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [SubmissionsFiltersComponent],
  entryComponents: [SubmissionsFiltersComponent],
  imports: [CommonModule, GeMaterialModule, FormsModule],
  providers: [SubmissionsFiltersService]
})
export class SubmissionsFiltersModule {}
