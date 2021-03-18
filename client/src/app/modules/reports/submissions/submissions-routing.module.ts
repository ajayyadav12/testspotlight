import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SubmissionsComponent } from './submissions.component';

const routes: Routes = [
	{
		path: '',
		component: SubmissionsComponent,
		children: [
			{ path: 'table', component: SubmissionsComponent },
			{ path: 'calendar', component: SubmissionsComponent },
			{ path: 'parents', component: SubmissionsComponent },
		],
	},
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule],
})
export class SubmissionsRoutingModule {}
