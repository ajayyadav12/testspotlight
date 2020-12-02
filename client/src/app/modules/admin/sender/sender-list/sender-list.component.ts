import { Component, OnInit } from '@angular/core';
import { Sender } from '../Sender';
import { SenderService } from '../sender.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-sender-list',
  template: `
    <p-button label="New Sender" routerLink="/sender/0"></p-button>
    <p></p>
    <app-ge-table
      [value]="senders"
      [columns]="columns"
      routerLink="/sender/"
      (deleteRecord)="onDeleteRecord($event)"
      [loading]="loading"
    ></app-ge-table>
  `
})
export class SenderListComponent implements OnInit {
  senders: Sender[];
  columns = [
    { field: 'name', header: 'Name' },
    { field: 'closePhaseName', header: 'Close Phase' },
    { field: 'appOwnerName', header: 'App Owner' }
  ];
  loading = false;
  constructor(private senderSvc: SenderService, private msgSvc: MessageService) {}

  ngOnInit() {
    this.loading = true;
    this.senderSvc.getAllSenders().subscribe(value => {
      this.loading = false;
      this.senders = value;
      this.senders.map(s => {
        s.appOwnerName = s.appOwner ? s.appOwner.name : '';
        s.closePhaseName = s.closePhase ? s.closePhase.name : '';
      });
    });
  }

  onDeleteRecord(id) {
    this.senderSvc.deleteSender(id).subscribe(value => {
      this.senders = this.senders.filter(s => {
        return s.id !== value.id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Sender was deleted`
      });
    });
  }
}
