import { HttpClient } from '@angular/common/http';

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvConfigurationService } from './env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class AuditLogService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	newAuditLog(moduleName: String): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/auditlog/${moduleName}`;
		return this.http.post(url, moduleName);
	}
}
