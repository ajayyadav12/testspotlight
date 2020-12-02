import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../notification.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-notification-list',
  template: `
    <p-button label="New Template" routerLink="/notification/0"></p-button>
    <p></p>
    <app-ge-table
      [value]="templates"
      [columns]="columns"
      routerLink="/notification/"
      (deleteRecord)="onDeleteRecord($event)"
      [loading]="loading"
    ></app-ge-table>
  `
})
export class NotificationListComponent implements OnInit {
  templates: any[];
  columns = [{ field: 'id', header: 'ID' }, { field: 'name', header: 'Name' }, { field: 'subject', header: 'Subject' }];
  loading = false;
  constructor(private notificatioSvc: NotificationService, private msgSvc: MessageService) {}

  ngOnInit() {
    this.loading = true;
    this.notificatioSvc.getNotificationTemplates().subscribe(value => {
      this.templates = value;
      this.loading = false;
    });
  }

  onDeleteRecord(id) {
    this.notificatioSvc.deleteNotificationtemplate(id).subscribe(value => {
      this.templates = this.templates.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Template was deleted`
      });
    });
  }
}
