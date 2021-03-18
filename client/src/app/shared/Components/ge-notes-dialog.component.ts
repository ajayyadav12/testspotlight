import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { UserService } from 'src/app/modules/admin/user/user.service';
import { ActivatedRoute } from '@angular/router';

@Component({
	selector: 'app-ge-notes-dialog',
	template: `
		<div class="p-grid p-fluid">
			<div class="p-col-12">
				<textarea
					pInputTextarea
					[disabled]="noteData.flag"
					[(ngModel)]="noteData.note"
					autofocus
					[autofocus]
				></textarea>
			</div>
			<div class="p-col-6">
				<section *ngIf="noteData.date">
					<div style="color:gray">{{ noteData.date | date: 'short' }}</div>
					<span><b>By:</b>&nbsp; {{ noteData.name }}</span>
				</section>
			</div>
			<div class="p-col-6">
				<div *ngIf="!noteData.date" style="float:right;">
					<button
						style="width: auto"
						pButton
						[disabled]="!noteData.note || noteData.flag"
						label="Submit"
						(click)="submit()"
					></button>
				</div>
			</div>
		</div>
	`,
})
export class GENotesDialogComponent implements OnInit {
	@Input() noteData: any = {
		flag: false,
		note: '',
		date: null,
		name: '',
	};
	userName;
	@Output() submitNote = new EventEmitter();
	constructor() {}

	ngOnInit() {}

	submit() {
		this.submitNote.emit(this.noteData.note);
		this.submitNote.emit(this.noteData.name);
		this.noteData.note = '';
		this.noteData.date = null;
		this.noteData.name = '';
	}
}
