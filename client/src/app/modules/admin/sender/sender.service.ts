import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SenderService {
  constructor(private http: HttpClient) {}

  deleteSender(id): Observable<any> {
    const url = environment.apiUrl + '/senders/' + id;
    return this.http.delete(url);
  }

  newSender(sender): Observable<any> {
    const url = environment.apiUrl + '/senders/';
    return this.http.post(url, sender);
  }

  getSender(id): Observable<any> {
    const url = environment.apiUrl + '/senders/' + id;
    return this.http.get(url);
  }

  getAllSenders(): Observable<any> {
    const url = environment.apiUrl + '/senders/';
    return this.http.get(url);
  }

  updateSender(id, sender): Observable<any> {
    const url = environment.apiUrl + '/senders/' + id;
    return this.http.put(url, sender);
  }
}
