import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class ScheduleService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	deleteSchedule(processId, scheduleId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-definitions/${scheduleId}`;
		return this.http.delete(url);
	}

	getSchedule(scheduleId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/processes/0/schedule-definitions/${scheduleId}`;
		return this.http.get(url);
	}

	getSchedules(processId, bypassAccess?: boolean): Observable<any> {
		const include = bypassAccess ? '/all' : '';
		const url = this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-definitions` + include;
		return this.http.get(url);
	}

	newSchedule(processId, schedule: any): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-definitions`;
		return this.http.post(url, schedule);
	}

	updateSchedule(processId, scheduleId, schedule: any): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-definitions/${scheduleId}`;
		return this.http.put(url, schedule);
	}

	deleteCriticalSchedule(processId, scheduleId): Observable<any> {
		const url =
			this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-critical-definitions/${scheduleId}`;
		return this.http.delete(url);
	}

	getCriticalSchedule(scheduleId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/processes/0/schedule-critical-definitions/${scheduleId}`;
		return this.http.get(url);
	}

	getCriticalSchedules(processId, bypassAccess?: boolean): Observable<any> {
		const include = bypassAccess ? '/all' : '';
		const url = this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-critical-definitions` + include;
		return this.http.get(url);
	}

	newCriticalSchedule(processId, schedule: any): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-critical-definitions`;
		return this.http.post(url, schedule);
	}

	updateCriticalSchedule(processId, scheduleId, schedule: any): Observable<any> {
		const url =
			this.envSvc.appConfig.apiURL + `/processes/${processId}/schedule-critical-definitions/${scheduleId}`;
		return this.http.put(url, schedule);
	}
}
