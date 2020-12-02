import { NgModule } from '@angular/core';
import { SidebarService } from './sidebar/sidebar.service';
import { SessionService } from './session/session.service';
import { MessageService } from 'primeng/api';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpConfigInterceptor } from './interceptor/httpconfig-interceptor';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

@NgModule({
  providers: [
    SidebarService,
    SessionService,
    MessageService,
    { provide: HTTP_INTERCEPTORS, useClass: HttpConfigInterceptor, multi: true },
    AuthGuard,
    RoleGuard
  ]
})
export class CoreModule {}
