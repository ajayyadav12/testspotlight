import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ScheduleSubmissionsService {
  constructor(private http: HttpClient) { }

  setAcknowledgementFlag(id: number, acknowledgementNote: string): Observable<any> {
    const url = `${environment.apiUrl}/scheduled-submissions/${id}/acknowledgement`;
    return this.http.post(url, { acknowledgementNote: acknowledgementNote });
  }

  setDisable(id: number, disabledNote: string): Observable<any> {
    let url;
    url = `${environment.apiUrl}/scheduled-submissions/${id}/disable`;
    return this.http.post(url, { acknowledgementNote: disabledNote });
  }

  getUpcomingSubmissions(scheduleDefId): Observable<any> {
    const url = environment.apiUrl + `/scheduled-submissions/${scheduleDefId}/scheduleSubmissionList`;
    return this.http.get(url);
  }

  getExpectedSubmissions(from, to): Observable<any> {
    const url = environment.apiUrl + '/scheduled-submissions/';
    return this.http.get(url, {
      params: {
        from: from,
        to: to
      }
    });
  }

  getScheduleSubmissions(params): Observable<any> {
    const url = environment.apiUrl + `/scheduled-submissions/filterSubmissions`;
    return this.http.get(url, {
      params: params
    });
  }

  updateUpcomingSubmission(scheduledSubmissionId, startTime, endTime): Observable<any> {
    let url;
    url = `${environment.apiUrl}/scheduled-submissions/${scheduledSubmissionId}/update`;
    return this.http.post(url, {}, { params: { from: startTime, to: endTime } });
  }
}
