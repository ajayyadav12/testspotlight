import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { Router } from '@angular/router';
import { EnvConfigurationService } from '../services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class SessionService {
	processName: any;
	globalProcessName: any;
	logOutURL = `https://ssologin.ssogen2.corporate.ge.com/logoff/logoff.jsp?referrer=${window.location.origin}/login?logout=true`;

	public get token(): string {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session) {
			return session.token;
		} else {
			return '';
		}
	}

	public get name(): string {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session) {
			return session.user.name;
		} else {
			return '';
		}
	}

	public get sso(): string {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session) {
			return session.user.sso;
		} else {
			return '';
		}
	}

	public get role(): string {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session) {
			return session.user.role.description;
		} else {
			return '';
		}
	}

	constructor(private envSvc: EnvConfigurationService, private http: HttpClient, private router: Router) {}

	logIn(user): Observable<any> {
		return this.http.post(this.envSvc.appConfig.apiURL + '/security/token', user);
	}

	isUserOfProcess(processId: number): boolean {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session) {
			return session.processes.find((p) => p == processId) != null;
		} else {
			return false;
		}
	}

	updateMyProcesses(processes: Array<number>) {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session) {
			session.processes = processes;
			localStorage.setItem('session', JSON.stringify(session));
		}
	}

	/**
	 * Clear localstorage and redirect to SSO logout page to delete cookies.
	 */
	logout() {
		localStorage.clear();
		location.replace(this.logOutURL);
	}
}
