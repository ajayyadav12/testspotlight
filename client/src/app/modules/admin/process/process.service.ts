import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ProcessExportRequest } from './process-dtl/process-dtl-export/ProcessExportRequest';
import { Process } from './Process';
import { NotificationRequest } from './process-dtl/process-dtl-notifications/NotificationRequest';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class ProcessService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	approveProcess(id, check: boolean): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/approve';
		return this.http.post(url, { check: check });
	}

	deleteProcess(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id;
		return this.http.delete(url);
	}

	deleteProcessNotification(processId, notificationId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + processId + '/notifications/' + notificationId;
		return this.http.delete(url);
	}

	deleteProcessMyNotification(processId, notificationId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + processId + '/my-notifications/' + notificationId;
		return this.http.delete(url);
	}

	deleteProcessStep(processId, stepId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + processId + '/steps/' + stepId;
		return this.http.delete(url);
	}

	deleteProcessUser(processId, userId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + processId + '/users/' + userId;
		return this.http.delete(url);
	}

	newProcess(process: any): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/';
		return this.http.post(url, process);
	}

	newProcessStep(id: any, step: any): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/steps';
		return this.http.post(url, step);
	}

	updateProcessStep(processId: any, processStepId: any, step: any): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + processId + '/steps/' + processStepId;
		return this.http.put(url, step);
	}

	newProcessNotification(id, processNotification): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/notifications';
		return this.http.post(url, processNotification);
	}

	newProcessMyNotification(processId: number, myNotification: NotificationRequest): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/processes/${processId}/my-notifications`;
		return this.http.post(url, myNotification);
	}

	newProcessUser(id, userId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/users';
		return this.http.post(url, {
			userId: userId,
		});
	}

	getAllProcesses(bypassAccess?: boolean): Observable<any> {
		const include = bypassAccess ? 'all' : '';
		const url = this.envSvc.appConfig.apiURL + '/processes/' + include;
		return this.http.get(url);
	}

	getAssignableProcesses(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/assignable';
		return this.http.get(url);
	}

	getProcessList(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/list';
		return this.http.get(url);
	}

	getProcessChildren(id): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/processes/${id}/children-map`;
		return this.http.get(url);
	}

	updateProcessMap(id, process): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/processes/${id}/children-map`;
		return this.http.post(url, process);
	}

	deleteProcessMap(id, childId): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/processes/${id}/children-map/${childId}`;
		return this.http.delete(url, {});
	}

	getProcess(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id;
		return this.http.get(url);
	}

	updateProcess(id, process): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id;
		return this.http.put(url, process);
	}

	updateProcessAlerts(id, alertSettings): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/alerts/';
		return this.http.put(url, alertSettings);
	}

	updateProcessNotification(processId: number, notificationId: number, notificationPayload): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/processes/${processId}/notifications/${notificationId}`;
		return this.http.put(url, notificationPayload);
	}

	updateProcessMyNotification(
		processId: number,
		notificationId: number,
		notificationPayload: NotificationRequest
	): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/processes/${processId}/my-notifications/${notificationId}`;
		return this.http.patch(url, notificationPayload);
	}

	getAllProcessTypes(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/process-types/';
		return this.http.get(url);
	}

	getFeedTypes(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/feed-types/';
		return this.http.get(url);
	}

	getAllProcessSteps(id, bypassAccess?: boolean): Observable<any> {
		const include = bypassAccess ? '/all' : '';
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/steps' + include;
		return this.http.get(url);
	}

	getProcessNotifications(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/my-notifications';
		return this.http.get(url);
	}

	getProcessUsers(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/users';
		return this.http.get(url);
	}

	getToken(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/token';
		return this.http.post(url, {});
	}

	getChildren(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/processes/' + id + '/children';
		return this.http.get(url);
	}

	getExportRequests(processId: number): Observable<Array<ProcessExportRequest>> {
		return this.http.get<Array<ProcessExportRequest>>(
			`${this.envSvc.appConfig.apiURL}/processes/${processId}/exports`
		);
	}

	createExportRequest(processId: number, settings: String[]): Observable<ProcessExportRequest> {
		return this.http.post<ProcessExportRequest>(`${this.envSvc.appConfig.apiURL}/processes/${processId}/exports`, {
			settings,
		});
	}

	approveExportRequest(processId: number, exportRequestId: number, notes: string): Observable<ProcessExportRequest> {
		return this.http.patch<ProcessExportRequest>(
			`${this.envSvc.appConfig.apiURL}/processes/${processId}/exports/${exportRequestId}/approve`,
			{ notes }
		);
	}

	declineExportRequest(processId: number, exportRequestId: number, notes: string): Observable<ProcessExportRequest> {
		return this.http.patch<ProcessExportRequest>(
			`${this.envSvc.appConfig.apiURL}/processes/${processId}/exports/${exportRequestId}/decline`,
			{ notes }
		);
	}

	downloadExport(processId: number, exportRequestId: number): Observable<any> {
		return this.http.get(
			`${this.envSvc.appConfig.apiURL}/processes/${processId}/exports/${exportRequestId}/export`
		);
	}

	uploadImport(processId: number, importFile: any): Observable<Process> {
		return this.http.post<Process>(`${this.envSvc.appConfig.apiURL}/processes/${processId}/import`, importFile);
	}

	copyProcess(processId: number, name: string, settings: String[]): Observable<Process> {
		return this.http.post<Process>(`${this.envSvc.appConfig.apiURL}/processes/${processId}/copy`, {
			name,
			settings,
		});
	}

	myProcesses(): Observable<Array<number>> {
		return this.http.get<Array<number>>(`${this.envSvc.appConfig.apiURL}/my-processes/`);
	}

	getProcessSubmitPermission(): Observable<Array<Process>> {
		return this.http.get<Array<Process>>(`${this.envSvc.appConfig.apiURL}/processes/submit-permission`);
	}
}
