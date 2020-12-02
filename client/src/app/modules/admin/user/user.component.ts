import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-user',
  template: `
    <router-outlet></router-outlet>
  `
})
export class UserComponent implements OnInit {
  constructor(private sidebarSvc: SidebarService) {}

  ngOnInit() {
    this.sidebarSvc.title = 'Users';
  }
}
