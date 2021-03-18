import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable()
export class GEFiltersService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getAllSenders(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/systems/';
		return this.http.get(url);
	}

	getAllProcesses(bypassAccess?: boolean): Observable<any> {
		const include = bypassAccess ? 'all' : '';
		const url = this.envSvc.appConfig.apiURL + '/processes/' + include;
		return this.http.get(url);
	}
}
