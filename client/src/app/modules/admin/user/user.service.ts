import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable({
	providedIn: 'root',
})
export class UserService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getUser(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/' + id;
		return this.http.get(url);
	}

	getUsers(mobileUsersOnly = false): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/';
		return this.http.get(url, {
			params: {
				mobileUsersOnly: mobileUsersOnly ? 'true' : 'false',
			},
		});
	}

	newUser(user): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/';
		return this.http.post(url, user);
	}

	updateUser(id, user): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/' + id;
		return this.http.put(url, user);
	}

	deleteUser(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/' + id;
		return this.http.delete(url);
	}

	getCarriers(): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/message-gateway/';
		return this.http.get(url);
	}

	getUserPermissions(id): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/' + id + '/permissions';
		return this.http.get(url);
	}

	grantPermission(id, permission): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/' + id + '/permission';
		return this.http.post(url, permission);
	}

	removePermission(id, userPermissionId): Observable<any> {
		const url = this.envSvc.appConfig.apiURL + '/users/' + id + '/permission/' + userPermissionId;
		return this.http.delete(url);
	}
}
