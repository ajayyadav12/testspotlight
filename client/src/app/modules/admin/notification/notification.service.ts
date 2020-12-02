import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable()
export class NotificationService {
  constructor(private http: HttpClient) {}

  deleteNotificationtemplate(id): Observable<any> {
    const url = environment.apiUrl + '/notification-templates/' + id;
    return this.http.delete(url);
  }

  getNotificationTemplate(id): Observable<any> {
    const url = environment.apiUrl + '/notification-templates/' + id;
    return this.http.get(url);
  }

  getNotificationTemplates(): Observable<any> {
    const url = environment.apiUrl + '/notification-templates/';
    return this.http.get(url);
  }

  newNotificationTemplate(template): Observable<any> {
    const url = environment.apiUrl + '/notification-templates/';
    return this.http.post(url, template);
  }

  updateNotificationTemplate(id, template): Observable<any> {
    const url = environment.apiUrl + '/notification-templates/' + id;
    return this.http.put(url, template);
  }
}
