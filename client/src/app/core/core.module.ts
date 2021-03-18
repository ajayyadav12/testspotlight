import { APP_INITIALIZER, NgModule } from '@angular/core';
import { SidebarService } from './sidebar/sidebar.service';
import { SessionService } from './session/session.service';
import { MessageService } from 'primeng/api';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpConfigInterceptor } from './interceptor/httpconfig-interceptor';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { DatePipe } from '@angular/common';
import { EnvConfigurationService } from './services/env-configuration.service';

@NgModule({
	providers: [
		SidebarService,
		SessionService,
		MessageService,
		{ provide: HTTP_INTERCEPTORS, useClass: HttpConfigInterceptor, multi: true },
		{
			provide: APP_INITIALIZER,
			useFactory: (envConfigService: EnvConfigurationService) => () => envConfigService.load(),
			deps: [EnvConfigurationService],
			multi: true,
		},
		AuthGuard,
		RoleGuard,
		DatePipe,
	],
})
export class CoreModule {}
