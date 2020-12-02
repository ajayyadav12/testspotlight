import { LoginService } from './login.service';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SessionService } from 'src/app/core/services/session.service';

const response_type = environment.response_type;
const client_id = environment.client_id;
const redirect_uri = environment.redirect_uri;
const scope = environment.scope;
const authURL = environment.authURL;
const tokenURL = environment.tokenURL;
const client_secret = window.atob(environment.client_secret);
const grant_type = environment.grant_type;
const logOutURL = environment.logOutURL;

@Component({
  selector: 'ge-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logOutURL = logOutURL;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private loginSvc: LoginService,
    private _snackBar: MatSnackBar,
    private session: SessionService
  ) { }

  ngOnInit() {
    this.session.title = 'Log in';
    this.route.queryParams.subscribe(params => {
      if (params.code) {
        this.getTokenUsingCode(params.code);
      } else if (params.logout) {
      } else if (params.error_description) {
        this.loginFailed();
      }
    });
  }

  /**
   * After being authenticated in SSO server, now let's verify if user is an spotlight user
   * @param code Code received from SSO server
   */
  getTokenUsingCode(code: any): any {
    const url = this.getTokenURL(code);
    this.loginSvc.logIn({ sso: '', password: '', url: url, source: 'mobile' }).subscribe(
      value => {
        localStorage.setItem('session', JSON.stringify(value));
        this.router.navigate(['/submissions']);
        this._snackBar.open('Welcome back!');
      },
      e => {
        this.loginFailed();
      }
    );
  }

  getAuthURL(): string {
    return `${authURL}?response_type=${response_type}&client_id=${client_id}&redirect_uri=${redirect_uri}&scope=${scope}`;
  }

  getTokenURL(code): string {
    return `${tokenURL}?grant_type=${grant_type}&code=${code}&redirect_uri=${redirect_uri}&client_id=${client_id}&client_secret=${client_secret}`;
  }

  goToSSOAuthPage() {
    const url = this.getAuthURL();
    location.replace(url);
  }

  loginFailed() {
    this.router.navigate(['/login'], { queryParams: { logout: true } });
    this._snackBar.open('Something failed!');
  }
}
