import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class SubmissionsFiltersService {
  constructor(private http: HttpClient) {}

  getAllProcesses(): Observable<any> {
    const url = `${environment.apiUrl}/processes/all`;
    return this.http.get(url);
  }
}
