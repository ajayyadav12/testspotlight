import { Component, OnInit } from '@angular/core';
import { Receiver } from '../Receiver';
import { ReceiverService } from '../receiver.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-receiver-list',
  template: `
    <p-button label="New Receiver" routerLink="/receiver/0"></p-button>
    <p></p>
    <app-ge-table
      [value]="receivers"
      [columns]="columns"
      routerLink="/receiver/"
      (deleteRecord)="onDeleteRecord($event)"
      [loading]="loading"
    ></app-ge-table>
  `
})
export class ReceiverListComponent implements OnInit {
  receivers: Receiver[];
  columns = [{ field: 'name', header: 'Name' }, { field: 'closePhaseName', header: 'Close Phase' }];
  loading = false;
  constructor(private receiverSvc: ReceiverService, private msgSvc: MessageService) {}

  ngOnInit() {
    this.loading = true;
    this.receiverSvc.getAllReceiver().subscribe(value => {
      this.receivers = value;
      this.receivers.map(r => {
        r.closePhaseName = r.closePhase ? r.closePhase.name : '';
      });
      this.loading = false;
    });
  }

  onDeleteRecord(id) {
    this.receiverSvc.deleteReceiver(id).subscribe(value => {
      this.receivers = this.receivers.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Receiver was deleted`
      });
    });
  }
}
