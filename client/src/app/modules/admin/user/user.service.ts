import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient) {}

  getUser(id): Observable<any> {
    const url = environment.apiUrl + '/users/' + id;
    return this.http.get(url);
  }

  getUsers(): Observable<any> {
    const url = environment.apiUrl + '/users/';
    return this.http.get(url);
  }

  newUser(user): Observable<any> {
    const url = environment.apiUrl + '/users/';
    return this.http.post(url, user);
  }

  updateUser(id, user): Observable<any> {
    const url = environment.apiUrl + '/users/' + id;
    return this.http.put(url, user);
  }

  deleteUser(id): Observable<any> {
    const url = environment.apiUrl + '/users/' + id;
    return this.http.delete(url);
  }

  getCarriers(): Observable<any> {
    const url = environment.apiUrl + '/message-gateway/';
    return this.http.get(url);
  }
}
