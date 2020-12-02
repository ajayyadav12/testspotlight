import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-sender',
  template: `
    <router-outlet></router-outlet>
  `
})
export class SenderComponent implements OnInit {
  constructor(private sidebarSvc: SidebarService) {}

  ngOnInit() {
    this.sidebarSvc.title = 'Sender';
  }
}
