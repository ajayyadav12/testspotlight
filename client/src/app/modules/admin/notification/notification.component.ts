import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-notification',
  template: `
    <router-outlet></router-outlet>
  `,
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {
  constructor(private sidebarSvc: SidebarService) {}

  ngOnInit() {
    this.sidebarSvc.title = 'Notification';
  }
}
