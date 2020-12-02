import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuditLogService {
    constructor(private http: HttpClient) { }

    newAuditLog(moduleName: String): Observable<any> {
        const url = `${environment.apiUrl}/auditlog/${moduleName}`;
        return this.http.post(url, moduleName);
    }
}
