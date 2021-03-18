import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable()
export class NotificationService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	deleteNotificationtemplate(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/' + id;
		return this.http.delete(url);
	}

	getNotificationTemplate(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/' + id;
		return this.http.get(url);
	}

	getNotificationTemplates(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/';
		return this.http.get(url);
	}

	newNotificationTemplate(template): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/';
		return this.http.post(url, template);
	}

	updateNotificationTemplate(id, template): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/' + id;
		return this.http.put(url, template);
	}

	updateUserNotification(uniqueId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/updateUserNotificationStatus/' + uniqueId;
		return this.http.put(url, uniqueId);
	}

	getUserNotifications(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/notification-templates/getUserNotifications';
		return this.http.get(url);
	}
}
