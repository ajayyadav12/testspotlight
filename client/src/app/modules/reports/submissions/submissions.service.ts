import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class SubmissionsService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getSubmissions(params?): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionCount(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-count';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionCountByProcess(params, status): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-count/' + status;
		return this.http.get(url, {
			params: params,
		});
	}

	getParentSubmissions(params?): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/parentSubmissions/';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionSteps(id: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/submissions/${id}/steps`;
		return this.http.get(url);
	}

	getSubmissionParent(parentId: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/parentSubmissions/${parentId}`;
		return this.http.get(url);
	}

	manualSubmissionClosing(submission, manualCloseObj): Observable<any> {
		let url;
		url = `${this.envSvc.appConfig.apiURL}/submissions/${submission.submissionId}/manual-close`;
		return this.http.post(url, {
			status: manualCloseObj.status,
			notes: manualCloseObj.notes,
			appsApiURL: this.envSvc.appConfig.appsApiUrl,
			processId: submission.processId,
		});
	}

	setAcknowledgementFlag(id: number, acknowledgementNote: string, acknowledgedBy: string): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/submissions/${id}/acknowledgement`;
		return this.http.post(url, { acknowledgementNote: acknowledgementNote, acknowledgedBy: acknowledgedBy });
	}

	getSubmissionInProgress(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-drill-in-progress';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionFailed(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-drill-failed';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionDelayed(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-drill-delayed';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionWarning(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-drill-warning';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionSuccess(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-drill-success';
		return this.http.get(url, {
			params: params,
		});
	}

	getSubmissionReports(submissionId: number, flag: string): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/submissions/data-report-file/${submissionId}/${flag}`;
		return this.http.get(url);
	}

	getSubmissionCurrentStatus(params): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/submissions/submission-current-status';
		return this.http.get(url, {
			params: params,
		});
	}

	uploadSubmissionFile(formData: FormData): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/submissions/file`;
		return this.http.post(url, formData);
	}
}
