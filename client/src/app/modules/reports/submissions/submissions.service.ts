import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SubmissionsService {
  constructor(private http: HttpClient) { }

  getSubmissions(params?): Observable<any> {
    const url = environment.apiUrl + '/submissions/';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionCount(params): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-count';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionCountByProcess(params, status): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-count/' + status;
    return this.http.get(url, {
      params: params
    });
  }

  getParentSubmissions(params?): Observable<any> {
    const url = environment.apiUrl + '/parentSubmissions/';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionSteps(id: number): Observable<any> {
    const url = `${environment.apiUrl}/submissions/${id}/steps`;
    return this.http.get(url);
  }

  getSubmissionParent(parentId: number): Observable<any> {
    const url = `${environment.apiUrl}/parentSubmissions/${parentId}`;
    return this.http.get(url);
  }

  manualSubmissionClosing(submission, manualCloseObj): Observable<any> {
    let url;
    url = `${environment.apiUrl}/submissions/${submission.submissionId}/manual-close`;
    return this.http.post(url, {
      status: manualCloseObj.status,
      notes: manualCloseObj.notes,
      appsApiURL: environment.appsApiUrl,
      processId: submission.processId
    });
  }

  setAcknowledgementFlag(id: number, acknowledgementNote: string): Observable<any> {
    const url = `${environment.apiUrl}/submissions/${id}/acknowledgement`;
    return this.http.post(url, { acknowledgementNote: acknowledgementNote });
  }

  getSubmissionInProgress(params): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-drill-in-progress';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionFailed(params): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-drill-failed';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionDelayed(params): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-drill-delayed';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionWarning(params): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-drill-warning';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionSuccess(params): Observable<any> {
    const url = environment.apiUrl + '/submissions/submission-drill-success';
    return this.http.get(url, {
      params: params
    });
  }

}
