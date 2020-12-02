import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-ge-notes-dialog',
  template: `
    <textarea pInputTextarea [disabled]="noteData.flag" [(ngModel)]="noteData.note" autofocus [autofocus]></textarea>
    <span *ngIf="noteData.date">Date: {{ noteData.date | date: 'short' }}</span> <br /><br />
    <p-button [disabled]="!noteData.note || noteData.flag" label="Submit" (onClick)="submit()"></p-button>
  `
})
export class GENotesDialogComponent implements OnInit {
  @Input() noteData: any = {
    flag: false,
    note: '',
    date: null
  };
  @Output() submitNote = new EventEmitter();
  constructor() {}

  ngOnInit() {}

  submit() {
    this.submitNote.emit(this.noteData.note);
    this.noteData.note = '';
    this.noteData.date = null;
  }
}
