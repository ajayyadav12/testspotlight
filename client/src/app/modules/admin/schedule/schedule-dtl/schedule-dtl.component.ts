import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

const TODAY = new Date();
@Component({
  selector: 'app-schedule-dtl',
  templateUrl: './schedule-dtl.component.html',
  styleUrls: ['./schedule-dtl.component.scss'],
})
export class ScheduleDtlComponent implements OnInit {
  processType;
  scheduleMenu = [{ label: 'Summary' }, { label: 'Upcoming submissions' }];
  uniqueId;
  constructor(private route: ActivatedRoute) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.processType = params['delayed'];
      this.uniqueId = params['uniqueId'];
    });
  }
}
