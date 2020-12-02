import { Component, OnInit } from '@angular/core';
import { SidebarService } from './core/sidebar/sidebar.service';
import { SessionService } from './core/session/session.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  msgs;

  get isLoggedIn(): boolean {
    if (this.sessionSvc.token) {
      return true;
    } else {
      return false;
    }
  }

  get listPage(): string {
    return '/' + location.pathname.split('/')[1];
  }

  get isDtlPage(): boolean {
    return location.pathname.match('[0-9]') != null;
  }

  constructor(public sidebarSvc: SidebarService, private sessionSvc: SessionService) {
    this.sidebarSvc.reload(this.sessionSvc.role);
  }

  ngOnInit(): void {
    if (screen.width < 500) {
      location.assign('mobile');
    }
  }
}
