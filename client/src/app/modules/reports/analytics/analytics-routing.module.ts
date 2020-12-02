import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AnalyticsComponent } from './analytics.component';
import { TrendComponent } from './trend/trend.component';
import { VarianceComponent } from './variance/variance.component';
import { StatusComponent } from './status/status.component';

const routes: Routes = [
  {
    path: '',
    component: AnalyticsComponent,
    children: [
      { path: '', redirectTo: 'trending', pathMatch: 'full' },
      { path: 'trending', component: TrendComponent },
      { path: 'variance', component: VarianceComponent },
      { path: 'status', component: StatusComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AnalyticsRoutingModule {}
