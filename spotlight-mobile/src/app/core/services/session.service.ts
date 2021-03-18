import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  logOutURL = `https://ssologin.ssogen2.corporate.ge.com/logoff/logoff.jsp?referrer=${window.location.origin}/login?logout=true`;
  public title = '';

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

  constructor() { }

  /**
   * Clear localstorage and redirect to SSO logout page to delete cookies.
   */
  logout() {
    localStorage.clear();
    location.replace(this.logOutURL);
  }
}
