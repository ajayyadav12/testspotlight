import { Component, OnInit } from '@angular/core';
import { SessionService } from 'src/app/core/session/session.service';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Component({
	selector: 'app-login',
	templateUrl: './login.component.html',
	styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
	logOutURL;
	redirectURI = `${window.location.origin}/login`;

	constructor(
		private sessionSvc: SessionService,
		private router: Router,
		private msgSvc: MessageService,
		private sidebarSvc: SidebarService,
		private route: ActivatedRoute,
		private envSvc: EnvConfigurationService
	) {
		this.logOutURL = this.sessionSvc.logOutURL;
		this.route.queryParams.subscribe((params) => {
			if (params.code) {
				this.getTokenUsingCode(params.code);
			} else if (params.logout) {
			} else if (params.error_description) {
				this.loginFailed();
			} else {
				this.goToSSOAuthPage();
			}
		});
	}

	ngOnInit() {
		this.sidebarSvc.title = 'Log in';
	}

	/**
	 * After being authenticated in SSO server, now let's verify if user is an spotlight user
	 * @param code Code received from SSO server
	 */
	getTokenUsingCode(code: any): any {
		this.sessionSvc.logIn({ redirectURI: this.redirectURI, code, source: 'System' }).subscribe(
			(value) => {
				localStorage.setItem('session', JSON.stringify(value));
				this.msgSvc.add({
					severity: 'success',
					summary: 'So it begins...',
					detail: `Welcome back!`,
				});
				this.msgSvc.clear('persist');
				this.sidebarSvc.reload(value.user.role.description);
				this.router.navigate(['dashboard']);
			},
			(e) => {
				this.loginFailed();
			}
		);
	}

	getAuthURL(): string {
		return `https://fssfed.ge.com/fss/as/authorization.oauth2?response_type=code&client_id=${this.envSvc.appConfig.oidcClientId}&redirect_uri=${this.redirectURI}&scope=profile openid api`;
	}

	goToSSOAuthPage() {
		const url = this.getAuthURL();
		location.replace(url);
	}

	loginFailed() {
		this.router.navigate(['/login'], { queryParams: { logout: true } });
		this.msgSvc.clear('persist');
		this.msgSvc.add({
			severity: 'error',
			summary: 'oh oh!',
			detail: `User or password is not valid`,
			key: 'persist',
		});
	}
}
