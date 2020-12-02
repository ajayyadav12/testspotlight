import { Component, OnInit } from '@angular/core';
import { ProcessService } from '../process.service';
import { Process } from '../Process';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { MessageService } from 'primeng/api';
import { SessionService } from 'src/app/core/session/session.service';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

@Component(
  {
    providers: [AuditLogService],
    selector: 'app-process-list',
    template: `
    <p-button *ngIf="isAdmin || isApplication" label="New Process" routerLink="/process/0"></p-button>
    <p></p>
    <app-ge-table
      [value]="processes"
      [columns]="columns"
      routerLink="/process/"
      (deleteRecord)="onDeleteRecord($event)"
      [loading]="loading"
      iconColumn="name"
    ></app-ge-table>
  `
  }
)
export class ProcessListComponent implements OnInit {
  processes: Process[];
  columns = [
    { field: 'name', header: 'Name' },
    { field: 'senderName', header: 'Sender' },
    { field: 'receiverName', header: 'Receiver' },
    { field: 'processTypeName', header: 'Process Type' },
    { field: 'processParentName', header: 'Process Parent' },
    { field: 'appOwnerName', header: 'App Owner' },
    { field: 'critical', header: 'Critical' }
  ];
  loading = false;

  get isAdmin() {
    return this.sessionSvc.role === 'admin';
  }

  get isApplication() {
    return this.sessionSvc.role === 'application';
  }

  constructor(
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private sidebarSvc: SidebarService,
    private sessionSvc: SessionService,
    private auditLogSvc: AuditLogService
  ) {
    this.sidebarSvc.title = 'Process';
    this.auditLogSvc.newAuditLog('Process List').subscribe(value => { });;
  }

  ngOnInit() {
    this.loading = true;
    this.processSvc.getAllProcesses().subscribe(value => {
      this.processes = value;
      this.processes.map(p => {
        p.senderName = p.sender ? p.sender.name : '';
        p.receiverName = p.receiver ? p.receiver.name : '';
        p.processTypeName = p.processType ? p.processType.name : '';
        p.processParentName = p.processParent ? p.processParent.name : '';
        p.appOwnerName = p.appOwner ? p.appOwner.name : '';
        p.iconClass = p.approved === 'N' ? 'pi pi-times' : p.approved === '0' ? 'pi pi-clock' : '';
        p.iconColor = p.approved === 'N' ? 'red' : p.approved === '0' ? 'orange' : '';
      });
      this.loading = false;
    });
  }

  onDeleteRecord(id) {
    this.processSvc.deleteProcess(id).subscribe(value => {
      this.processes = this.processes.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Process was deleted`
      });
    });
  }
}
