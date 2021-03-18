import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AnalyticsComponent } from './analytics.component';
import { TrendComponent } from './trend/trend.component';

const routes: Routes = [
  {
    path: '',
    component: AnalyticsComponent,
    children: [
      { path: '', redirectTo: 'trending', pathMatch: 'full' },
      { path: 'trending', component: TrendComponent },
      { path: 'variance', loadChildren: () => import('./variance/variance.module').then(m => m.VarianceModule) },
      { path: 'status', loadChildren: () => import('./status/status.module').then(m => m.StatusModule) },
      { path: 'systems-load', loadChildren: () => import('./systems-load/systems-load.module').then(m => m.SystemsLoadModule) }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AnalyticsRoutingModule { }
