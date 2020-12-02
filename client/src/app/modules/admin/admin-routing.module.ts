import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { RoleGuard } from 'src/app/core/guards/role.guard';

const routes: Routes = [
  {
    path: 'schedule',
    loadChildren: () => import('./schedule/schedule.module').then(m => m.ScheduleModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'sender',
    loadChildren: () => import('./sender/sender.module').then(m => m.SenderModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['admin'] }
  },
  {
    path: 'receiver',
    loadChildren: () => import('./receiver/receiver.module').then(m => m.ReceiverModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['admin'] }
  },
  {
    path: 'process',
    loadChildren: () => import('./process/process.module').then(m => m.ProcessModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user',
    loadChildren: () => import('./user/user.module').then(m => m.UserModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['admin'] }
  },
  {
    path: 'notification',
    loadChildren: () => import('./notification/notification.module').then(m => m.NotificationModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['admin'] }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {}
