import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ClosePhaseService {
  constructor(private http: HttpClient) {}

  getClosePhases(): Observable<any> {
    const url = environment.apiUrl + '/close-phases/';
    return this.http.get(url);
  }
}
