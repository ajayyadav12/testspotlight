import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  public get token(): string {
    const session = JSON.parse(localStorage.getItem('session'));
    if (session) {
      return session.token;
    } else {
      return '';
    }
  }

  public get name(): string {
    const session = JSON.parse(localStorage.getItem('session'));
    if (session) {
      return session.user.name;
    } else {
      return '';
    }
  }

  public get sso(): string {
    const session = JSON.parse(localStorage.getItem('session'));
    if (session) {
      return session.user.sso;
    } else {
      return '';
    }
  }

  public get role(): string {
    const session = JSON.parse(localStorage.getItem('session'));
    if (session) {
      if (session.user) {
        return session.user.role.description;
      } else {
        localStorage.clear();
      }
    } else {
      return '';
    }
  }

  constructor(private http: HttpClient, private router: Router) {}

  logIn(user): Observable<any> {
    return this.http.post(environment.apiUrl + '/security/token', user);
  }

  /**
   * Clear localstorage and redirect to SSO logout page to delete cookies.
   */
  logout() {
    localStorage.clear();
    location.replace(environment.logOutURL);
  }
}
