import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: '/submissions'
  },
  {
    path: 'submissions',
    loadChildren: () => import('./modules/submissions/submissions.module').then(m => m.SubmissionsModule),
    canActivate: [AuthGuard]
  },
  { path: 'login', loadChildren: () => import('./modules/login/login.module').then(m => m.LoginModule) }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
