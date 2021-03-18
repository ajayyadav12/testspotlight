import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class AnalyticsService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getChildProcessSubmissions(id, params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/varianceReport/';
		return this.http.get(url, {
			params: {
				processId: id,
				from: params.default ? null : params.from,
				to: params.default ? null : params.to,
				bu: params.bu,
			},
		});
	}

	getParentProcessSubmissions(id, params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/parentSubmissions/list';
		return this.http.get(url, {
			params: {
				processId: id,
				from: params.default ? null : params.from,
				to: params.default ? null : params.to,
			},
		});
	}

	newScheduledReport(processId, schedule): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/summaryreport/${processId}/schedulesummaryreport`;
		return this.http.post(url, schedule);
	}

	getAllScheduledReports() {
		const url = this.envSvc.appConfig.apiURL + `/summaryreport/list`;
		return this.http.get(url);
	}

	deleteScheduledReport(reportId, processId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/summaryreport/${reportId}/analyticsReport/${processId}`;
		return this.http.delete(url);
	}
}
