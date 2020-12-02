import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {
  constructor(private sidebarSvc: SidebarService, private sessionSvc: SessionService) {
    this.sidebarSvc.title = 'Account';
  }

  ngOnInit() {}

  logout() {
    this.sessionSvc.logout();
  }
}
