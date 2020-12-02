import { Component, OnInit, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-schedule-dtl-weekly',
  templateUrl: './schedule-dtl-weekly.component.html',
  styleUrls: ['./schedule-dtl-weekly.component.scss']
})
export class ScheduleDtlWeeklyComponent implements OnInit {
  @Input() scheduleForm: FormGroup;
  @Input() days: any[];
  constructor() {}

  ngOnInit() {}
}
