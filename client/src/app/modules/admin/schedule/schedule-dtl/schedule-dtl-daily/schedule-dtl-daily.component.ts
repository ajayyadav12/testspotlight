import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-schedule-dtl-daily',
  templateUrl: './schedule-dtl-daily.component.html',
  styleUrls: ['./schedule-dtl-daily.component.scss']
})
export class ScheduleDtlDailyComponent implements OnInit {
  @Input() scheduleForm: FormGroup;
  @Output() dailyOptionChanged = new EventEmitter();
  timeRecurrences = [{ label: 'Every hour', value: 1 }];
  constructor() {}

  ngOnInit() {
    for (let index = 2; index < 24; index++) {
      this.timeRecurrences.push({ label: `Every ${index} hours`, value: index });
    }
  }

  onChangeIsEveryWeekday(event) {
    const dailyRecurrence = this.scheduleForm.get('dailyRecurrence');
    if (event) {
      dailyRecurrence.get('recurEvery').setValue(null);
      dailyRecurrence.get('recurEvery').disable();
    } else {
      dailyRecurrence.get('recurEvery').enable();
    }
  }

  onClickDailyOption() {
    this.dailyOptionChanged.emit();
  }
}
