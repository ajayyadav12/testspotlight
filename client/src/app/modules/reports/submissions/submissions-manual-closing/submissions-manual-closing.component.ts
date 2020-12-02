import { SessionService } from './../../../../core/session/session.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { SubmissionsService } from '../submissions.service';

@Component({
  selector: 'app-submissions-manual-closing',
  templateUrl: './submissions-manual-closing.component.html',
  styleUrls: ['./submissions-manual-closing.component.scss']
})
export class SubmissionsManualClosingComponent implements OnInit {
  @Input() submission: {submissionId: number, processId: number};
  @Output() submissionClose = new EventEmitter();

  manualCloseForm: FormGroup;

  constructor(private submissionSvc: SubmissionsService, private fb: FormBuilder) {
    this.manualCloseForm = this.fb.group({
      notes: [null, Validators.required],
      status: ['warning', Validators.required]
    });
  }

  ngOnInit() {}

  submit() {
    this.submissionSvc.manualSubmissionClosing(this.submission, this.manualCloseForm.value).subscribe(value => {
      this.submissionClose.emit(value);
    });
  }
}
