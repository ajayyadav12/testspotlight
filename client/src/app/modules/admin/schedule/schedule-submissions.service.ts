import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class ScheduleSubmissionsService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	setAcknowledgementFlag(id: number, acknowledgementNote: string, acknowledgedBy: string): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/scheduled-submissions/${id}/acknowledgement`;
		return this.http.post(url, { acknowledgementNote: acknowledgementNote, acknowledgedBy: acknowledgedBy });
	}

	setDisable(id: number, disabledNote: string): Observable<any> {
		let url;
		url = `${this.envSvc.appConfig.apiURL}/scheduled-submissions/${id}/disable`;
		return this.http.post(url, { acknowledgementNote: disabledNote });
	}

	getUpcomingSubmissions(scheduleDefId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/scheduled-submissions/${scheduleDefId}/scheduleSubmissionList`;
		return this.http.get(url);
	}

	getExpectedSubmissions(from, to): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/scheduled-submissions/';
		return this.http.get(url, {
			params: {
				from: from,
				to: to,
			},
		});
	}

	getScheduleSubmissions(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/scheduled-submissions/filterSubmissions`;
		return this.http.get(url, {
			params: params,
		});
	}

	getScheduleSubmission(submissionId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/scheduled-submissions/${submissionId}`;
		return this.http.get(url);
	}

	updateUpcomingSubmission(scheduledSubmissionId, startTime, endTime, editNotes): Observable<any> {
		let url;
		url = `${this.envSvc.appConfig.apiURL}/scheduled-submissions/${scheduledSubmissionId}/update`;
		return this.http.post(url, {}, { params: { from: startTime, to: endTime, notes: editNotes } });
	}
}
