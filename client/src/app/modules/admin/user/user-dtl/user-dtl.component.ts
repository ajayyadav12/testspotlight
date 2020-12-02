import { Component, OnInit } from '@angular/core';
import { Validators, FormGroup, FormBuilder } from '@angular/forms';
import { UserService } from '../user.service';
import { MessageService } from 'primeng/api';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-user-dtl',
  templateUrl: './user-dtl.component.html',
  styleUrls: ['./user-dtl.component.scss']
})
export class UserDtlComponent implements OnInit {
  userForm: FormGroup;
  carriers = [];
  roles = [
    { label: 'Admin', value: { id: 1, description: 'admin' } },
    { label: 'User', value: { id: 2, description: 'user' } },
    { label: 'Application', value: { id: 3, description: 'application' } }
  ];
  id;
  constructor(
    private userSvc: UserService,
    private fb: FormBuilder,
    private msgSvc: MessageService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.userForm = this.fb.group({
      id: [{ value: null, disabled: true }],
      name: ['', Validators.required],
      sso: ['', Validators.required],
      role: [null, Validators.required],
      carrier: null,
      phoneNumber: null,
      email: [{ value: null, disabled: true }]
    });
    this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.id !== 0) {
      this.getUser(this.id);
    }
  }

  ngOnInit() {}

  getUser(id) {
    this.userSvc.getUser(id).subscribe(value => {
      this.carriers = [value.carrier];
      this.userForm.setValue(value);
    });
  }

  onShowCarriers(event) {
    this.userSvc.getCarriers().subscribe(value => {
      this.carriers = value;
    });
  }

  save() {
    if (this.id !== 0) {
      this.userSvc.updateUser(this.id, this.userForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Fresh look!',
          detail: `User '${value.name}' updated`
        });
        this.router.navigate(['/user']);
      });
    } else {
      this.userSvc.newUser(this.userForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Welcome to the family!',
          detail: `User '${value.name}' created`
        });
        this.router.navigate(['/user']);
      });
    }
  }
}
