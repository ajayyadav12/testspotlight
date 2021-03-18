import { HttpClient } from '@angular/common/http';

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvConfigurationService } from './env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class ClosePhaseService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getClosePhases(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/close-phases/';
		return this.http.get(url);
	}
}
