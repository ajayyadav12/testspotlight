import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardOpsFiltersComponent } from './dashboard-ops-filters.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { FormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';
import { RelationshipModule } from 'src/app/modules/relationship/relationship.module';

@NgModule({
  declarations: [DashboardOpsFiltersComponent],
  imports: [CommonModule, PrimengModule, FormsModule, SharedModule, RelationshipModule],
  exports: [DashboardOpsFiltersComponent]
})
export class DashboardOpsFiltersModule {}
