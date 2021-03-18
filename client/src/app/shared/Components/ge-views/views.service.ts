import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable()
export class ViewsService {
	constructor(private envSvc: EnvConfigurationService, private http: HttpClient) {}

	getViews(moduleName: string): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/module-filters/${moduleName}`;
		return this.http.get(url);
	}

	deleteView(id: number): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/module-filters/${id}`;
		return this.http.delete(url);
	}

	saveView(view): Observable<any> {
		const url = `${this.envSvc.appConfig.apiURL}/module-filters/`;
		return this.http.post(url, view);
	}

	updateMultiple(views): Observable<any> {
		return this.http.put(`${this.envSvc.appConfig.apiURL}/module-filters/`, views);
	}
}
