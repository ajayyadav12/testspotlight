import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-receiver',
  template: `
    <router-outlet></router-outlet>
  `
})
export class ReceiverComponent implements OnInit {
  constructor(private sidebarSvc: SidebarService) {}

  ngOnInit() {
    this.sidebarSvc.title = 'Receiver';
  }
}
