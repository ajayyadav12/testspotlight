import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-process',
  template: `
    <router-outlet></router-outlet>
  `
})
export class ProcessComponent implements OnInit {
  constructor(private sidebarSvc: SidebarService) {}

  ngOnInit() {
    this.sidebarSvc.title = 'Process';
  }
}
