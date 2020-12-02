import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-schedule',
  template: `
    <router-outlet></router-outlet>
  `
})
export class ScheduleComponent implements OnInit {
  constructor(private siderbarSvc: SidebarService) {}

  ngOnInit() {
    this.siderbarSvc.title = 'Schedule';
  }
}
