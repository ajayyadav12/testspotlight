import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable()
export class RelationshipService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getSystemsByClosePhase(closePhaseId: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/systems/close-phase/${closePhaseId}`;
		return this.http.get(url);
	}

	getSystemRelationships(systemId: number, timeOption: string): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/systems/relationships/${systemId}`;
		return this.http.get(url, {
			params: { timeOption: timeOption },
		});
	}

	getSubmission(id: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/submissions/${id}`;
		return this.http.get(url);
	}

	getParentSubmission(id: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/parentSubmissions/${id}`;
		return this.http.get(url);
	}

	getAverageValue(submissionId: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/submissions/getAverageValue/${submissionId}`;
		return this.http.get(url);
	}
}
