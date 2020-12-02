import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  constructor(private http: HttpClient) {}

  deleteSchedule(processId, scheduleId): Observable<any> {
    const url = environment.apiUrl + `/processes/${processId}/schedule-definitions/${scheduleId}`;
    return this.http.delete(url);
  }

  getSchedule(scheduleId): Observable<any> {
    const url = environment.apiUrl + `/processes/0/schedule-definitions/${scheduleId}`;
    return this.http.get(url);
  }

  getSchedules(processId, bypassAccess?: boolean): Observable<any> {
    const include = bypassAccess ? '/all' : '';
    const url = environment.apiUrl + `/processes/${processId}/schedule-definitions` + include;
    return this.http.get(url);
  }

  newSchedule(processId, schedule: any): Observable<any> {
    const url = environment.apiUrl + `/processes/${processId}/schedule-definitions`;
    return this.http.post(url, schedule);
  }

  updateSchedule(processId, scheduleId, schedule: any): Observable<any> {
    const url = environment.apiUrl + `/processes/${processId}/schedule-definitions/${scheduleId}`;
    return this.http.put(url, schedule);
  }
}
