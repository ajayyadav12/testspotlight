import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardFiltersComponent } from './dashboard-filters.component';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { FormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';

@NgModule({
  declarations: [DashboardFiltersComponent],
  imports: [CommonModule, PrimengModule, FormsModule, SharedModule],
  exports: [DashboardFiltersComponent]
})
export class DashboardFiltersModule {}
