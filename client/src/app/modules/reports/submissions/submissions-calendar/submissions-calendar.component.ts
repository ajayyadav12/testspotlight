import { Component, OnInit, Input } from '@angular/core';
import { ScheduleSubmissionsService } from 'src/app/modules/admin/schedule/schedule-submissions.service';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
@Component({
  selector: 'app-submissions-calendar',
  templateUrl: './submissions-calendar.component.html',
  styleUrls: ['./submissions-calendar.component.scss']
})
export class SubmissionsCalendarComponent implements OnInit {
  options: any;
  @Input() submissions: any[];
  @Input() submissionCalender: boolean;
  @Input() calendarViews = 'dayGridMonth,timeGridWeek,timeGridDay';
  expectedSubmissions = [];


  /**
   * Combine both dataset to return current and future submissions.
   */
  get calendarSubmission() {
    if (this.submissionCalender) {
      return {
        events: this.expectedSubmissions,
        eventClick: function (info) {
          console.log(info);
        }
      };
    } else {
      return {
        events: this.submissions.concat(this.expectedSubmissions),
        eventClick: function (info) {
          console.log(info);
        }
      };
    }
  }

  get calendarScheduledSubmission() {
    return {
      events: this.expectedSubmissions,
      eventClick: function (info) {
        console.log(info);
      }
    };
  }

  constructor(private scheduleSubmissionsSvc: ScheduleSubmissionsService) { }

  ngOnInit() {
    this.getExpectedSubmissions();
    this.setCalendarOptions();
  }

  getExpectedSubmissions() {
    const date1 = new Date(new Date().setDate(new Date().getDate())).toISOString().split('T')[0];
    const date2 = new Date(new Date().setDate(new Date().getDate() + 90)).toISOString().split('T')[0];
    this.scheduleSubmissionsSvc.getExpectedSubmissions(date1, date2).subscribe((value: any[]) => {
      this.expectedSubmissions = value;
      this.expectedSubmissions.map(s => {
        s.title = s.process.name;
        s.start = new Date(s.startTime);
        s.end = new Date(s.endTime);
        if (this.calendarSubmission) {
          s.color = 'lightsteelblue';
        } else {
          s.color = 'lightgray';
        }

      });
    });
  }

  setCalendarOptions() {
    if (this.submissionCalender) {
      this.options = {
        plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
        defaultDate: new Date(),
        defaultView: 'timeGridDay',
        contentHeight: 'auto',
        header: {
          left: 'prev,next',
          center: 'title',
          right: this.calendarViews,
        },
        editable: false
      };
    } else {
      this.options = {
        plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
        defaultDate: new Date(),
        contentHeight: 'auto',
        header: {
          left: 'prev,next',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        editable: false
      };
    }
  }
}
