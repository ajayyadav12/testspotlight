import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from 'src/app/core/guards/auth.guard';

const routes: Routes = [
	{
		path: 'submissions',
		loadChildren: () => import('./submissions/submissions.module').then((m) => m.SubmissionsModule),
		canActivate: [AuthGuard]
	},
	{
		path: 'analytics',
		loadChildren: () => import('./analytics/analytics.module').then((m) => m.AnalyticsModule),
		canActivate: [AuthGuard]
	},
	{
		path: 'sla-dashboard',
		loadChildren: () => import('./sla/sla.module').then((m) => m.SlaModule),
		canActivate: [AuthGuard]
	}
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class ReportsRoutingModule {}
