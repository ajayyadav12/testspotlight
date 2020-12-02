import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProcessService {
  constructor(private http: HttpClient) { }

  approveProcess(id, check: boolean): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/approve';
    return this.http.post(url, { check: check });
  }

  deleteProcess(id): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id;
    return this.http.delete(url);
  }

  deleteProcessNotification(processId, notificationId): Observable<any> {
    const url = environment.apiUrl + '/processes/' + processId + '/notifications/' + notificationId;
    return this.http.delete(url);
  }

  deleteProcessStep(processId, stepId): Observable<any> {
    const url = environment.apiUrl + '/processes/' + processId + '/steps/' + stepId;
    return this.http.delete(url);
  }

  deleteProcessUser(processId, userId): Observable<any> {
    const url = environment.apiUrl + '/processes/' + processId + '/users/' + userId;
    return this.http.delete(url);
  }

  newProcess(process: any): Observable<any> {
    const url = environment.apiUrl + '/processes/';
    return this.http.post(url, process);
  }

  newProcessStep(id: any, stepName: string, stepRequired: boolean, stepParallel: boolean): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/steps';
    return this.http.post(url, {
      name: stepName,
      required: stepRequired,
      parallel: stepParallel,
      duration: 0
    });
  }

  newProcessNotification(id, processNotification): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/notifications';
    return this.http.post(url, processNotification);
  }

  newProcessUser(id, userId): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/users';
    return this.http.post(url, {
      userId: userId
    });
  }

  getAllProcesses(bypassAccess?: boolean): Observable<any> {
    const include = bypassAccess ? 'all' : '';
    const url = environment.apiUrl + '/processes/' + include;
    return this.http.get(url);
  }

  getProcessList(): Observable<any> {
    const url = environment.apiUrl + '/processes/list';
    return this.http.get(url);
  }

  getProcessChildren(id): Observable<any> {
    const url = `${environment.apiUrl}/processes/${id}/children-map`;
    return this.http.get(url);
  }

  updateProcessMap(id, process): Observable<any> {
    const url = `${environment.apiUrl}/processes/${id}/children-map`;
    return this.http.post(url, process);
  }

  deleteProcessMap(id, childId): Observable<any> {
    const url = `${environment.apiUrl}/processes/${id}/children-map/${childId}`;
    return this.http.delete(url, {});
  }

  getProcess(id): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id;
    return this.http.get(url);
  }

  updateProcess(id, process): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id;
    return this.http.put(url, process);
  }

  updateProcessAlerts(id, longRunningSub, escalatedEmails, longRunningStep): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/alerts/';
    return this.http.put(url, {
      processId: id,
      longRunningSubmission: longRunningSub,
      failedEscalation: escalatedEmails,
      longRunningStep: longRunningStep
    });
  }

  getAllProcessTypes(): Observable<any> {
    const url = environment.apiUrl + '/process-types/';
    return this.http.get(url);
  }

  getFeedTypes(): Observable<any> {
    const url = environment.apiUrl + '/feed-types/';
    return this.http.get(url);
  }

  getAllProcessSteps(id, bypassAccess?: boolean): Observable<any> {
    const include = bypassAccess ? '/all' : '';
    const url = environment.apiUrl + '/processes/' + id + '/steps' + include;
    return this.http.get(url);
  }

  getProcessNotifications(id): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/notifications';
    return this.http.get(url);
  }

  getProcessUsers(id): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/users';
    return this.http.get(url);
  }

  getToken(id): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/token';
    return this.http.post(url, {});
  }

  getChildren(id): Observable<any> {
    const url = environment.apiUrl + '/processes/' + id + '/children';
    return this.http.get(url);
  }
}
