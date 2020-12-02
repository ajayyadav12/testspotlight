import { Component, AfterViewInit, Input } from '@angular/core';
import { FormGroup, FormArray, Validators, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-schedule-dtl-custom',
  templateUrl: './schedule-dtl-custom.component.html',
  styleUrls: ['./schedule-dtl-custom.component.scss']
})
export class ScheduleDtlCustomComponent implements AfterViewInit {
  @Input() scheduleForm: FormGroup;
  @Input() incomingDates?: any[];
  get datesArray() {
    return this.scheduleForm['controls'].customRecurrence as FormArray;
  }
  dates: string[];

  constructor(private fb: FormBuilder) {}

  ngAfterViewInit() {
    if (this.incomingDates) {
      setTimeout(() => {
        this.loadDates();
      });
    }
  }

  loadDates() {
    this.incomingDates.forEach(d => {
      this.datesArray.push(
        this.fb.group({
          startTime: d.startTime,
          endTime: d.endTime
        })
      );
    });
  }

  addDate() {
    this.datesArray.push(
      this.fb.group({
        startTime: [null, Validators.required],
        endTime: [null, Validators.required]
      })
    );
  }

  onClickDeleteDate(index) {
    this.datesArray.removeAt(index);
  }

  onClickImport(ev) {
    if (confirm(`${this.dates.length} entries will be loaded. Do you like to continue?`)) {
      // Clean up file entries for no duplicates
      this.dates = this.dates.filter(function(x, i, a) {
        return a.indexOf(x) === i;
      });

      this.dates.forEach(d => {
        const dateRecord = d.split(',');
        this.datesArray.push(
          this.fb.group({
            startTime: new Date(dateRecord[0]),
            endTime: new Date(dateRecord[1])
          })
        );
      });
    }
  }

  onFileSelect(input: HTMLInputElement) {
    const files = input.files;

    if (files && files.length) {
      const fileToRead = files[0];

      const fileReader = new FileReader();
      fileReader.onload = ev => {
        const textFromFileLoaded: string = ev.currentTarget['result'];
        this.dates = textFromFileLoaded.split(/\r\n|\n|\r/);
      };

      fileReader.readAsText(fileToRead, 'UTF-8');
    }
  }
}
