import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class SystemService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	deleteSystem(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/systems/' + id;
		return this.http.delete(url);
	}

	newSystem(system): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/systems/';
		return this.http.post(url, system);
	}

	getSystem(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/systems/' + id;
		return this.http.get(url);
	}

	getAllSystems(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/systems/';
		return this.http.get(url);
	}

	updateSystem(id, system): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/systems/' + id;
		return this.http.put(url, system);
	}
}
