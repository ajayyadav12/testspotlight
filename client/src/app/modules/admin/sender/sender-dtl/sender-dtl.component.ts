import { ClosePhaseService } from './../../../../core/services/close-phase.service';
import { UserService } from './../../user/user.service';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { SenderService } from '../sender.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-sender-dtl',
  templateUrl: './sender-dtl.component.html',
  styleUrls: ['./sender-dtl.component.scss']
})
export class SenderDtlComponent implements OnInit {
  senderForm: FormGroup;
  users = [];
  closePhases = [];
  id;
  constructor(
    private senderSvc: SenderService,
    private fb: FormBuilder,
    private msgSvc: MessageService,
    private route: ActivatedRoute,
    private userSvc: UserService,
    private router: Router,
    private closePhaseSvc: ClosePhaseService
  ) {
    this.senderForm = this.fb.group({
      id: [{ value: null, disabled: true }],
      name: ['', Validators.required],
      appOwner: [null, Validators.required],
      closePhase: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.id !== 0) {
      this.getSender(this.id);
    }
    this.getUsers();
    this.getClosePhases();
  }

  getSender(id) {
    this.senderSvc.getSender(id).subscribe(value => {
      this.senderForm.setValue(value);
    });
  }

  getUsers() {
    this.userSvc.getUsers().subscribe(value => {
      this.users = value;
    });
  }

  getClosePhases() {
    this.closePhaseSvc.getClosePhases().subscribe(value => {
      this.closePhases = value;
    });
  }

  save() {
    if (this.id) {
      this.senderSvc.updateSender(this.id, this.senderForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Sender '${value.name}' was updated`
        });
        this.router.navigate(['/sender']);
      });
    } else {
      this.senderSvc.newSender(this.senderForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Oh we have a new sender!',
          detail: `Sender '${value.name}' created`
        });
        this.router.navigate(['/sender']);
      });
    }
  }
}
