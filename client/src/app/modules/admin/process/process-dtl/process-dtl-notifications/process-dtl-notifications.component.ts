import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { NotificationService } from '../../../notification/notification.service';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../../user/user.service';

@Component({
  selector: 'app-process-dtl-notifications',
  templateUrl: './process-dtl-notifications.component.html',
  styleUrls: ['./process-dtl-notifications.component.scss'],
  providers: [NotificationService, UserService]
})
export class ProcessDtlNotificationsComponent implements OnInit {
  processId;
  steps = [];
  dls = [];
  templates = [];
  status = [
    { name: 'In progress', id: 1 },
    { name: 'Success', id: 2 },
    { name: 'Warning', id: 3 },
    { name: 'Failed', id: 4 }
    // { label: 'Fatal', value: 5 } // Defect : DE112003
  ];
  processNotifications = [];
  users = [];
  processNotificationForm: FormGroup;
  columns = [
    { field: 'id', header: 'ID' },
    { field: 'processStepName', header: 'Step' },
    { field: 'statusName', header: 'Status' },
    { field: 'additionalEmails', header: 'Additional Emails' },
    { field: 'createdForPhoneNumber', header: 'Phone Number' }
  ];

  longRunningSub = false;
  escalatedEmails = false;
  longRunningStep = false;

  get input() {
    return this.processNotificationForm ? this.processNotificationForm.value : null;
  }

  constructor(
    private fb: FormBuilder,
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private route: ActivatedRoute,
    private userSvc: UserService
  ) {
    this.setupForm();
    this.onChangeLevel();
  }

  setupForm() {
    this.processNotificationForm = this.fb.group({
      level: 'delayed',
      processSteps: null,
      status: null,
      additionalEmails: '',
      enableTextMessaging: false,
      createdFor: [{ value: null, disabled: true }, Validators.required]
    });
    this.processNotificationForm.get('enableTextMessaging').valueChanges.subscribe(value => {
      this.onChangeEnableTextMessaging(value);
    });
    this.processNotificationForm.get('level').valueChanges.subscribe(value => {
      this.onChangeLevel();
    });
  }

  ngOnInit() {
    this.processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.processId !== 0) {
      this.getProcessNotifications();
      this.getProcessAlerts();
    }
  }

  getNotificationPayload(form, processStepIndex) {
    return {
      processStep: form.processSteps && form.processSteps.length > 0 ? form.processSteps[processStepIndex] : null,
      status: form.status,
      additionalEmails: form.additionalEmails,
      enableTextMessaging: form.enableTextMessaging,
      createdFor: form.createdFor
    };
  }

  addNotification() {
    const length = this.processNotificationForm.value.processSteps
      ? this.processNotificationForm.value.processSteps.length
      : 1;
    for (let index = 0; index < length; index++) {
      this.processSvc
        .newProcessNotification(this.processId, this.getNotificationPayload(this.processNotificationForm.value, index))
        .subscribe(value => {
          this.processNotificationForm.reset();
          this.notificationMapping(value);
          this.processNotifications.push(value);
        });
    }
    setTimeout(_ => {
      this.msgSvc.add({
        severity: 'success',
        summary: 'New notification setting!',
        detail: `Notification was added`
      });
    }, 500);
  }

  updateAlerts() {
    this.processSvc
      .updateProcessAlerts(this.processId, this.longRunningSub, this.escalatedEmails, this.longRunningStep)
      .subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Process alerts '${value.name}' were updated`
        });
      });
  }

  getProcessNotifications() {
    this.processSvc.getProcessNotifications(this.processId).subscribe(value => {
      this.processNotifications = value;
      this.processNotifications.map(pn => {
        this.notificationMapping(pn);
      });
    });
  }

  notificationMapping(pn) {
    pn.processStepName = pn.processStep ? pn.processStep.name : '';
    pn.statusName = pn.status ? pn.status.name : 'Delayed';
    pn.createdForPhoneNumber = pn.createdFor ? pn.createdFor.phoneNumber : '';
  }

  onChangeLevel() {
    this.processNotificationForm.controls['processSteps'].setValue(null);

    switch (this.input.level) {
      case 'delayed':
        this.processNotificationForm.controls['status'].setValue(null);
        this.processNotificationForm.controls['status'].disable();
        this.processNotificationForm.controls['processSteps'].disable();
        break;
      case 'statusStep':
        this.processNotificationForm.controls['processSteps'].enable();
        this.processNotificationForm.controls['status'].enable();
        break;
      case 'statusProcess':
        this.processNotificationForm.controls['status'].enable();
        this.processNotificationForm.controls['processSteps'].disable();
        break;
      default:
        break;
    }
  }

  onChangeEnableTextMessaging(value) {
    console.log(value);
    if (value) {
      this.processNotificationForm.controls['createdFor'].enable();
    } else {
      this.processNotificationForm.controls['createdFor'].setValue(null);
      this.processNotificationForm.controls['createdFor'].disable();
    }
  }

  onFocusProcessStep(event) {
    this.processSvc.getAllProcessSteps(this.processId).subscribe((value: any[]) => {
      this.steps = value;
    });
  }

  onShowCreatedFor(event) {
    this.userSvc.getUsers().subscribe(value => {
      this.users = value;
    });
  }

  onDeleteRecord(id) {
    this.processSvc.deleteProcessNotification(this.processId, id).subscribe(value => {
      this.processNotifications = this.processNotifications.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Notification was deleted`
      });
    });
  }

  getProcessAlerts() {
    this.processSvc.getProcess(this.processId).subscribe((process: any) => {
      this.longRunningSub = process.longRunningSubAlrt;
      this.escalatedEmails = process.submissionEscalationAlrt;
      this.longRunningStep = process.longRunningStepAlrt;
    });
  }
}
