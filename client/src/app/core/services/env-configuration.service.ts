import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export enum Environment {
	Prod = 'prod',
	QA = 'qa',
	Dev = 'dev',
	Local = 'local',
}

interface Configuration {
	environment: Environment;
	apiURL: string;
	appsApiUrl: string;
	oidcOauthUri: string;
	oidcClientId: string;
	logoutUrl: string;
	version: string;
	instrumentationKey: string;
}

@Injectable({ providedIn: 'root' })
export class EnvConfigurationService {
	appConfig: Configuration;

	constructor(private http: HttpClient) {}

	load() {
		const apiUrl = '/assets/config/config.json';
		return this.http
			.get<Configuration>(apiUrl)
			.toPromise()
			.then((data) => {
				this.appConfig = data;
			});
	}
}
