import { LoginService } from './login.service';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SessionService } from 'src/app/core/services/session.service';

const client_id = environment.client_id;

@Component({
  selector: 'ge-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logOutURL;
  redirectURI = `${window.location.origin}/mobile/login`;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private loginSvc: LoginService,
    private _snackBar: MatSnackBar,
    private session: SessionService
  ) {
    this.logOutURL = this.session.logOutURL;
  }

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
    this.loginSvc.logIn({ redirectURI: this.redirectURI, code, source: 'mobile' }).subscribe(
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
    return `https://fssfed.ge.com/fss/as/authorization.oauth2?response_type=code&client_id=${client_id}&redirect_uri=${this.redirectURI}&scope=profile openid api`;
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
