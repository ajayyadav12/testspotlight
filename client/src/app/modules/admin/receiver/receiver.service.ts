import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReceiverService {
  constructor(private http: HttpClient) {}

  deleteReceiver(id): Observable<any> {
    const url = environment.apiUrl + '/receivers/' + id;
    return this.http.delete(url);
  }

  newReceiver(receiver): Observable<any> {
    const url = environment.apiUrl + '/receivers/';
    return this.http.post(url, receiver);
  }

  getAllReceiver(): Observable<any> {
    const url = environment.apiUrl + '/receivers/';
    return this.http.get(url);
  }

  getReceiver(id): Observable<any> {
    const url = environment.apiUrl + '/receivers/' + id;
    return this.http.get(url);
  }

  updateReceiver(id, receiver): Observable<any> {
    const url = environment.apiUrl + '/receivers/' + id;
    return this.http.put(url, receiver);
  }
}
