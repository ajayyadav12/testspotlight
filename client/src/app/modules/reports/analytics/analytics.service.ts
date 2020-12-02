import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  constructor(private http: HttpClient) {}

  getChildProcessSubmissions(id, params): Observable<any> {
    const url = environment.apiUrl + '/varianceReport/';
    return this.http.get(url, {
      params: {
        processId: id,
        from: params.default ? null : params.from,
        to: params.default ? null : params.to
      }
    });
  }

  getParentProcessSubmissions(id, params): Observable<any> {
    const url = environment.apiUrl + '/parentSubmissions/list';
    return this.http.get(url, {
      params: {
        processId: id,
        from: params.default ? null : params.from,
        to: params.default ? null : params.to
      }
    });
  }

  newScheduledReport(processId, schedule): Observable<any> {
    const url = environment.apiUrl + `/summaryreport/${processId}/schedulesummaryreport`;
    return this.http.post(url, schedule);
  }

  getAllScheduledReports() {
    const url = environment.apiUrl + `/summaryreport/list`;
    return this.http.get(url);
  }

  deleteScheduledReport(reportId, processId): Observable<any> {
    const url = environment.apiUrl + `/summaryreport/${reportId}/analyticsReport/${processId}`;
    return this.http.delete(url);
  }
}
