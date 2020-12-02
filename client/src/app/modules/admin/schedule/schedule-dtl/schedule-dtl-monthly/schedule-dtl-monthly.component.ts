import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-schedule-dtl-monthly',
  templateUrl: './schedule-dtl-monthly.component.html',
  styleUrls: ['./schedule-dtl-monthly.component.scss']
})
export class ScheduleDtlMonthlyComponent implements OnInit {
  @Input() scheduleForm: FormGroup;
  @Output() monthOptionChanged = new EventEmitter();

  days2 = [
    { label: 'Monday', value: 'Monday' },
    { label: 'Tuesday', value: 'Tuesday' },
    { label: 'Wednesday', value: 'Wednesday' },
    { label: 'Thursday', value: 'Thursday' },
    { label: 'Friday', value: 'Friday' },
    { label: 'Saturday', value: 'Saturday' },
    { label: 'Sunday', value: 'Sunday' }
  ];

  ocurrences = [
    { label: 'First', value: 1 },
    { label: 'Second', value: 2 },
    { label: 'Third', value: 3 },
    { label: 'Forth', value: 4 }
  ];

  LCDs = [
    { label: 'LCD-5', value: -5 },
    { label: 'LCD-4', value: -4 },
    { label: 'LCD-3', value: -3 },
    { label: 'LCD-2', value: -2 },
    { label: 'LCD-1', value: -1 },
    { label: 'LCD-0', value: 0 },
    { label: 'LCD+1', value: 1 },
    { label: 'LCD+2', value: 2 },
    { label: 'LCD+3', value: 3 },
    { label: 'LCD+4', value: 4 },
    { label: 'LCD+5', value: 5 },
    { label: 'LCD+6', value: 6 },
    { label: 'LCD+7', value: 7 },
    { label: 'LCD+8', value: 8 },
    { label: 'LCD+9', value: 9 },
    { label: 'LCD+10', value: 10 }
  ];
  constructor() {}

  ngOnInit() {}

  onClickMonthOption() {
    this.monthOptionChanged.emit();
  }
}
