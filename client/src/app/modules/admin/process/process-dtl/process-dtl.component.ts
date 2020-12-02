import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

const PROCESS_MENU = [
  { label: 'Summary' },
  { label: 'Steps' },
  { label: 'Notifications' },
  { label: 'Users' },
  { label: 'Chart' }
];
@Component({
  selector: 'app-process-dtl',
  templateUrl: './process-dtl.component.html',
  styleUrls: ['./process-dtl.component.scss'],
  providers: [AuditLogService]
})
export class ProcessDtlComponent implements OnInit {
  processMenu = [];

  constructor(
    private route: ActivatedRoute,
    private auditLogSvc: AuditLogService
  ) { this.auditLogSvc.newAuditLog('New-Modify Process').subscribe(value => { }); }

  ngOnInit() {
    const processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (processId === 0) {
      this.processMenu = [{ label: 'Summary' }];
    } else {
      Object.assign(this.processMenu, PROCESS_MENU);
    }
  }

  isTabActive(activeItem, tabIndex): boolean {
    return activeItem !== this.processMenu[tabIndex];
  }
}
