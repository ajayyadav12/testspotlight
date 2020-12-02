import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SubmissionsRoutingModule } from './submissions-routing.module';
import { SubmissionsComponent } from './submissions.component';
import { GeMaterialModule } from 'src/app/shared/ge-material/ge-material.module';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { SubmissionsMenuComponent } from './submissions-menu/submissions-menu.component';
import { SubmissionsStepsModule } from './submissions-steps/submissions-steps.module';
import { FormsModule } from '@angular/forms';
import { SubmissionsService } from './submissions.service';
import { SubmissionsFiltersModule } from './submissions-filters/submissions-filters.module';

@NgModule({
  declarations: [SubmissionsComponent, SubmissionsMenuComponent],
  imports: [
    CommonModule,
    SubmissionsRoutingModule,
    GeMaterialModule,
    InfiniteScrollModule,
    SubmissionsStepsModule,
    SubmissionsFiltersModule,
    FormsModule
  ],
  entryComponents: [SubmissionsMenuComponent],
  providers: [SubmissionsService]
})
export class SubmissionsModule {}
