import { Component, OnInit, Input } from '@angular/core';
import { ProcessService } from '../../../process.service';
import { MessageService } from 'primeng/api';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
  selector: 'app-process-dtl-notifications-alerts',
  templateUrl: './process-dtl-notifications-alerts.component.html',
  styleUrls: ['./process-dtl-notifications-alerts.component.scss']
})
export class ProcessDtlNotificationsAlertsComponent implements OnInit {

  @Input() processId = 0;

  alertSettings = {
    longRunningSubAlrt: false,
    submissionEscalationAlrt: false,
    longRunningStepAlrt: false,
    submissionDelayedEscalationAlrt: false,
    requiredStepAlrt: false
  };

  get isAdmin() {
    return this.sessionService.role === 'admin';
  }

  get canEdit() {
    return this.sessionService.isUserOfProcess(this.processId);
  }

  constructor(private processSvc: ProcessService, 
    private msgSvc: MessageService,
    private sessionService: SessionService) { }

  ngOnInit(): void {
    if (this.processId !== 0) {
      this.getProcessAlerts();
    }
  }

  updateAlerts() {
    this.processSvc
      .updateProcessAlerts(this.processId, this.alertSettings)
      .subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Process alerts '${value.name}' were updated`
        });
      });
  }

  getProcessAlerts() {
    this.processSvc.getProcess(this.processId).subscribe((process: any) => {
      this.alertSettings.submissionDelayedEscalationAlrt = process.submissionDelayedEscalationAlrt;
      this.alertSettings.longRunningSubAlrt = process.longRunningSubAlrt;
      this.alertSettings.submissionEscalationAlrt = process.submissionEscalationAlrt;
      this.alertSettings.longRunningStepAlrt = process.longRunningStepAlrt;
      this.alertSettings.requiredStepAlrt = process.requiredStepAlrt;
      this.msgSvc.add({
        severity: 'info',
        detail: `By default, all emails are sent to process support email address ${process.supportTeamEmail}`,
        key: 'supportEmailMsg'
      });
    });
  }

}
