import { Component, OnInit } from '@angular/core';
import { SessionService } from 'src/app/core/session/session.service';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { environment } from 'src/environments/environment';

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
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logOutURL = logOutURL;

  constructor(
    private sessionSvc: SessionService,
    private router: Router,
    private msgSvc: MessageService,
    private sidebarSvc: SidebarService,
    private route: ActivatedRoute
  ) {
    this.route.queryParams.subscribe(params => {
      if (params.code) {
        this.getTokenUsingCode(params.code);
      } else if (params.logout) {
      } else if (params.error_description) {
        this.loginFailed();
      } else {
        this.goToSSOAuthPage();
      }
    });
  }

  ngOnInit() {
    this.sidebarSvc.title = 'Log in';
  }

  /**
   * After being authenticated in SSO server, now let's verify if user is an spotlight user
   * @param code Code received from SSO server
   */
  getTokenUsingCode(code: any): any {
    const url = this.getTokenURL(code);
    this.sessionSvc.logIn({ sso: '', password: '', url: url, source: 'System' }).subscribe(
      value => {
        localStorage.setItem('session', JSON.stringify(value));
        this.msgSvc.add({
          severity: 'success',
          summary: 'So it begins...',
          detail: `Welcome back!`
        });
        this.msgSvc.clear('persist');
        this.sidebarSvc.reload(value.user.role.description);
        this.router.navigate(['dashboard']);
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
    this.msgSvc.clear('persist');
    this.msgSvc.add({
      severity: 'error',
      summary: 'oh oh!',
      detail: `User or password is not valid`,
      key: 'persist'
    });
  }
}
