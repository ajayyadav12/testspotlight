import { Component, OnInit } from '@angular/core';
import { User } from '../User';
import { UserService } from '../user.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-user-list',
  template: `
    <p-button label="New User" routerLink="/user/0"></p-button>
    <p></p>
    <app-ge-table
      [value]="users"
      [columns]="columns"
      [loading]="loading"
      routerLink="/user/"
      (deleteRecord)="onDeleteRecord($event)"
    ></app-ge-table>
  `
})
export class UserListComponent implements OnInit {
  loading = false;
  users: User[];
  columns = [{ field: 'name', header: 'Name' }, { field: 'sso', header: 'SSO' }, { field: 'roleName', header: 'Role' }];

  constructor(private userSvc: UserService, private msgSvc: MessageService) {}

  ngOnInit() {
    this.loading = true;
    this.userSvc.getUsers().subscribe(value => {
      this.users = value;
      this.users.map(u => {
        u.roleName = u.role.description;
      });
      this.loading = false;
    });
  }

  onDeleteRecord(id) {
    this.userSvc.deleteUser(id).subscribe(value => {
      this.users = this.users.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `User was deleted`
      });
    });
  }
}
