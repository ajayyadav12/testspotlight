import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveEnd, Router } from '@angular/router';
import { ApplicationInsights } from '@microsoft/applicationinsights-web';
import { filter } from 'rxjs/operators';
import { EnvConfigurationService } from './env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class MyMonitoringService {
	private routerSub;
	private appInsights = new ApplicationInsights({
		config: {
			instrumentationKey: this.encs.appConfig.instrumentationKey,
			autoTrackPageVisitTime: true,
			enableCorsCorrelation: true,
			enableRequestHeaderTracking: true,
			enableResponseHeaderTracking: true,
			correlationHeaderExcludedDomains: ['localhost:4200'],
		},
	});

	constructor(private router: Router, private http: HttpClient, private encs: EnvConfigurationService) {
		this.appInsights.loadAppInsights();
		this.routerSub = this.router.events
			.pipe(filter((event) => event instanceof ResolveEnd))
			.subscribe((event: ResolveEnd) => {
				const activatedComponent = this.getActivatedComponent(event.state.root);
				if (activatedComponent) {
					this.logPageView(
						`${activatedComponent.name} ${this.getRouteTemplate(event.state.root)}`,
						event.urlAfterRedirects
					);
				}
			});
	}

	setUserId(userId: string) {
		this.appInsights.setAuthenticatedUserContext(userId, null, true);
	}

	clearUserId() {
		this.appInsights.clearAuthenticatedUserContext();
	}

	logPageView(name?: string, url?: string) {
		// option to call manually
		this.appInsights.trackPageView({
			name: name,
			uri: url,
		});
	}

	logEvent(name: string, properties?: { [key: string]: any }) {
		this.appInsights.trackEvent({ name: name }, properties);
	}

	logMetric(name: string, average: number, properties?: { [key: string]: any }) {
		this.appInsights.trackMetric({ name: name, average: average }, properties);
	}

	logException(exception: Error, severityLevel?: number) {
		this.appInsights.trackException({ exception: exception, severityLevel: severityLevel });
	}

	logTrace(message: string, properties?: { [key: string]: any }) {
		this.appInsights.trackTrace({ message: message }, properties);
	}

	private getActivatedComponent(snapshot: ActivatedRouteSnapshot): any {
		if (snapshot.firstChild) {
			return this.getActivatedComponent(snapshot.firstChild);
		}

		return snapshot.component;
	}

	private getRouteTemplate(snapshot: ActivatedRouteSnapshot): string {
		let path = '';
		if (snapshot.routeConfig) {
			path += snapshot.routeConfig.path;
		}

		if (snapshot.firstChild) {
			return path + this.getRouteTemplate(snapshot.firstChild);
		}

		return path;
	}
}
