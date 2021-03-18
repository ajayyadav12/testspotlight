import { Component, OnInit } from '@angular/core';
import { SidebarService } from './core/sidebar/sidebar.service';
import { SessionService } from './core/session/session.service';
import { Router, NavigationEnd } from '@angular/router';
import { environment } from 'src/environments/environment';
import { MyMonitoringService } from './core/services/logging.service';
declare let ga: Function;
@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
	msgs;
	//gtag: Function;
	get isLoggedIn(): boolean {
		if (this.sessionSvc.token) {
			return true;
		} else {
			return false;
		}
	}

	constructor(
		public sidebarSvc: SidebarService,
		private sessionSvc: SessionService,
		public router: Router,
		public myMonitoringService: MyMonitoringService
	) {
		this.sidebarSvc.reload(this.sessionSvc.role);
		if (environment.analytics) {
			this.router.events.subscribe((event) => {
				if (event instanceof NavigationEnd) {
					ga('set', 'page', event.urlAfterRedirects);
					ga('send', 'pageview');
				}
			});
		}
	}

	ngOnInit(): void {
		if (screen.width < 500) {
			location.assign('mobile');
		}

		if (this.isIE()) {
			alert(
				`Using IE is ok but some Spotlight functionality won't work as expected. We highly recommend using Chrome, Firefox or Edge.`
			);
		}
	}

	isIE(): boolean {
		const ua = navigator.userAgent;
		/* MSIE used to detect old browsers and Trident used to newer ones*/
		const is_ie = ua.indexOf('MSIE ') > -1 || ua.indexOf('Trident/') > -1;

		return is_ie;
	}
}
