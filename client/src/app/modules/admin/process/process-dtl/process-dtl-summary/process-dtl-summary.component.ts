import { Receiver } from './../../../receiver/Receiver';
import { UserService } from './../../../user/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ReceiverService } from 'src/app/modules/admin/receiver/receiver.service';
import { SenderService } from 'src/app/modules/admin/sender/sender.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Component, OnInit, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
  selector: 'app-process-dtl-summary',
  templateUrl: './process-dtl-summary.component.html',
  styleUrls: ['./process-dtl-summary.component.scss']
})
export class ProcessDtlSummaryComponent implements OnInit {
  @Input() isVisible: boolean;
  processForm: FormGroup;

  // Dropdowns
  senders = [];
  users = [];
  receivers = [];
  processTypes = [];
  feedTypes = [];
  processes = [];
  parents = [];
  siblings = [];

  processToken = '';
  id: number;

  combosObservable = new Subject<boolean>();
  combosCount = 0;

  get isAdmin() {
    return this.sessionSvc.role === 'admin';
  }

  get isApplication() {
    return this.sessionSvc.role === 'application';
  }

  get submitButtonLabel() {
    if (this.id === 0) {
      return this.isAdmin ? 'Save' : 'Submit for Approval';
    } else {
      return 'Save';
    }
  }

  constructor(
    private processSvc: ProcessService,
    private senderSvc: SenderService,
    private receiverSvc: ReceiverService,
    private fb: FormBuilder,
    private msgSvc: MessageService,
    private route: ActivatedRoute,
    private sidebarSvc: SidebarService,
    private sessionSvc: SessionService,
    private userSvc: UserService,
    private router: Router
  ) {
    this.setupForm();
    this.setupCombos();
    this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'));
    if (this.id !== 0) {
      this.combosObservable.subscribe(value => {
        // Wait till all dropdowns are ready
        this.combosCount++;
        if (this.combosCount === 6) {
          this.getProcess(this.id);
        }
      });
    }
  }

  ngOnInit() {}

  ngOnDestroy() {
    this.combosObservable.unsubscribe();
  }

  setupForm() {
    this.processForm = this.fb.group({
      approved: '0',
      appOwner: null,
      critical: false,
      feedType: [null, Validators.required],
      functionalOwner: null,
      id: [{ value: null, disabled: true }],
      isParent: false,
      longRunningSubAlrt: null,
      longRunningStepAlrt: null,
      name: ['', Validators.required],
      processType: [null, Validators.required],
      processParent: null,
      predecessor: null,
      processLevel: null,
      receiver: [null, Validators.required],
      sender: [null, Validators.required],
      submissionEscalationAlrt: null,
      successor: null,
      supportTeamEmail: ['', [Validators.email, Validators.required]],
      technicalOwner: null,
      maxRunTimeHours: [0, Validators.min(0)],
      maxRunTimeMinutes: [0, Validators.min(0)]
    });
  }

  copyToken() {
    if (this.processForm.disabled) {
      return;
    }
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.processToken;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.msgSvc.add({
      severity: 'success',
      summary: 'Now you are ready!',
      detail: `Token has been copied`
    });
  }

  setupCombos() {
    this.senderSvc.getAllSenders().subscribe(value => {
      this.senders = value;
      this.combosObservable.next(true);
    });
    this.receiverSvc.getAllReceiver().subscribe((value: Receiver[]) => {
      this.receivers = value;
      this.combosObservable.next(true);
    });
    this.processSvc.getAllProcessTypes().subscribe((value: any[]) => {
      this.processTypes = value;
      this.combosObservable.next(true);
    });
    this.processSvc.getFeedTypes().subscribe((value: any[]) => {
      this.feedTypes = value;
      this.combosObservable.next(true);
    });
    this.processSvc.getAllProcesses().subscribe((value: any[]) => {
      this.parents = value.filter(v => v.isParent);
      this.processes = value.filter(v => !v.isParent);
      this.combosObservable.next(true);
    });
    this.userSvc.getUsers().subscribe(value => {
      this.users = value;
      this.combosObservable.next(true);
    });
  }

  getProcess(id: number) {
    this.processSvc.getProcess(id).subscribe((process: any) => {
      if (process.processParent) {
        this.onChangeProcessParent({ value: process.processParent });
      }
      this.siblings.push(process.successor);
      this.siblings.push(process.predecessor);
      this.sidebarSvc.title = 'Process: ' + process.name;
      this.processApprovalStatusAction(process.approved);
      this.processForm.setValue(process);
    });
  }

  processApprovalStatusAction(approved) {
    if (approved === '0') {
      this.processForm.disable();
      this.msgSvc.add({
        severity: 'warn',
        summary: 'Patience is a virtue...',
        detail: 'Spotlight Admin Team is reviewing this process',
        key: 'persist'
      });
    } else if (approved === 'N') {
      this.processForm.disable();
      this.msgSvc.add({
        severity: 'error',
        summary: 'Sorry not sorry',
        detail: 'Spotlight Admin Team rejected this process',
        key: 'persist'
      });
    }
  }

  onChangeProcessParent(event) {
    this.processSvc.getChildren(event.value.id).subscribe((values: any[]) => {
      this.siblings = values;
      this.siblings = this.siblings.filter(s => {
        return s.id !== this.processForm.getRawValue().id;
      });
    });
  }

  onChangeIsParent(event) {
    if (event) {
      this.processForm.get('processParent').setValue(null);
      this.processForm.get('processParent').disable();
    } else {
      this.processForm.get('processParent').enable();
      this.processForm.get('processParent').setValue(null);
    }
  }

  save() {
    if (this.id !== 0) {
      this.processSvc.updateProcess(this.id, this.processForm.getRawValue()).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Process '${value.name}' was updated`
        });
        this.router.navigate(['/process']);
      });
    } else {
      this.processSvc.newProcess(this.processForm.getRawValue()).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Congrats! A new process was added!',
          detail: `Process '${value.name}' created`
        });
        this.router.navigate(['/process']);
      });
    }
  }

  getProcessToken() {
    if (this.processForm.disabled) {
      return;
    }
    this.processSvc.getToken(this.processForm.getRawValue().id).subscribe(value => {
      this.processToken = value.token;
    });
  }
}
