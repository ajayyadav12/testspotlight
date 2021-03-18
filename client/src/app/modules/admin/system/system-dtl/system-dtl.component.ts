import { ClosePhaseService } from '../../../../core/services/close-phase.service';
import { UserService } from '../../user/user.service';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { SystemService } from '../system.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-system-dtl',
  templateUrl: './system-dtl.component.html',
  styleUrls: ['./system-dtl.component.scss']
})
export class SystemDtlComponent implements OnInit {
  systemForm: FormGroup;
  users = [];
  closePhases = [];
  id;
  constructor(
    private systemSvc: SystemService,
    private fb: FormBuilder,
    private msgSvc: MessageService,
    private route: ActivatedRoute,
    private userSvc: UserService,
    private router: Router,
    private closePhaseSvc: ClosePhaseService
  ) {
    this.systemForm = this.fb.group({
      id: [{ value: null, disabled: true }],
      name: ['', Validators.required],
      appOwner: [null, Validators.required],
      closePhase: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.id !== 0) {
      this.getSystem(this.id);
    }
    this.getUsers();
    this.getClosePhases();
  }

  getSystem(id) {
    this.systemSvc.getSystem(id).subscribe(value => {
      this.systemForm.setValue(value);
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
      this.systemSvc.updateSystem(this.id, this.systemForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `System '${value.name}' was updated`
        });
        this.router.navigate(['/system']);
      });
    } else {
      this.systemSvc.newSystem(this.systemForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Oh we have a new system!',
          detail: `System '${value.name}' created`
        });
        this.router.navigate(['/system', value.id]);
      });
    }
  }
}
