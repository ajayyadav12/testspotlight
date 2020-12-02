import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../notification.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-notification-dtl',
  templateUrl: './notification-dtl.component.html',
  styleUrls: ['./notification-dtl.component.scss']
})
export class NotificationDtlComponent implements OnInit {
  templateForm: FormGroup;
  fieldValues = [
    { label: 'Process Name', value: '${PROCESS_NAME}' },
    { label: 'Scheduled Start', value: '${SCHEDULE_START}' },
    { label: 'Actual Start', value: '${ACTUAL_START}' },
    { label: 'Actual End', value: '${ACTUAL_END}' },
    { label: 'Submission Status', value: '${SUBMISSION_STATUS}' }
  ];
  id: number;
  constructor(
    private notificationSvc: NotificationService,
    private fb: FormBuilder,
    private router: Router,
    private msgSvc: MessageService,
    private route: ActivatedRoute
  ) {
    this.templateForm = this.fb.group({
      id: [null, Validators.min(1)],
      name: ['', Validators.required],
      subject: ['', Validators.required],
      body: ['', Validators.required]
    });
    this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.id !== 0) {
      this.getNotificationTemplate(this.id);
    }
  }

  ngOnInit() {}

  getNotificationTemplate(id) {
    this.notificationSvc.getNotificationTemplate(id).subscribe(value => {
      this.templateForm.setValue(value);
    });
  }

  onOptionClick(fieldValue) {
    const body: string = this.templateForm.value.body;
    this.templateForm.get('body').setValue(body.substring(0, body.length - 4) + fieldValue + '</p>');
  }

  save() {
    if (this.id !== 0) {
      this.notificationSvc.updateNotificationTemplate(this.id, this.templateForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Process '${value.name}' was updated`
        });
        this.router.navigate(['/notification']);
      });
    } else {
      this.notificationSvc.newNotificationTemplate(this.templateForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'New template!',
          detail: `Template '${value.name}' created`
        });
        this.router.navigate(['/notification']);
      });
    }
  }
}
