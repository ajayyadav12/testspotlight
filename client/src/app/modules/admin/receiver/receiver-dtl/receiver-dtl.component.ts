import { ClosePhaseService } from './../../../../core/services/close-phase.service';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ReceiverService } from '../receiver.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-receiver-dtl',
  templateUrl: './receiver-dtl.component.html',
  styleUrls: ['./receiver-dtl.component.scss']
})
export class ReceiverDtlComponent implements OnInit {
  receiverForm: FormGroup;
  id;
  closePhases = [];

  constructor(
    private receiverSvc: ReceiverService,
    private fb: FormBuilder,
    private msgSvc: MessageService,
    private route: ActivatedRoute,
    private router: Router,
    private closePhaseSvc: ClosePhaseService
  ) {
    this.receiverForm = this.fb.group({
      id: [{ value: null, disabled: true }],
      name: ['', Validators.required],
      closePhase: [null, Validators.required]
    });
    this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.id !== 0) {
      this.getReceiver(this.id);
    }
  }

  ngOnInit() {
    this.getClosePhases();
  }

  getReceiver(id) {
    this.receiverSvc.getReceiver(id).subscribe(value => {
      this.receiverForm.setValue(value);
    });
  }

  getClosePhases() {
    this.closePhaseSvc.getClosePhases().subscribe(value => {
      this.closePhases = value;
    });
  }

  save() {
    if (this.id) {
      this.receiverSvc.updateReceiver(this.id, this.receiverForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Receiver '${value.name}' was updated`
        });
        this.router.navigate(['/receiver']);
      });
    } else {
      this.receiverSvc.newReceiver(this.receiverForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Oh we have a new receiver!',
          detail: `Receiver '${value.name}' created`
        });
        this.router.navigate(['/receiver']);
      });
    }
  }
}
