import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class GEFiltersService {
  constructor(private http: HttpClient) {}

  getAllSenders(): Observable<any> {
    const url = environment.apiUrl + '/senders/';
    return this.http.get(url);
  }

  getAllReceiver(): Observable<any> {
    const url = environment.apiUrl + '/receivers/';
    return this.http.get(url);
  }

  getAllProcesses(bypassAccess?: boolean): Observable<any> {
    const include = bypassAccess ? 'all' : '';
    const url = environment.apiUrl + '/processes/' + include;
    return this.http.get(url);
  }
}
