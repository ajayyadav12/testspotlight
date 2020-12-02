import { Component, OnInit, Input } from '@angular/core';
import { UserService } from '../../../user/user.service';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-process-dtl-users',
  templateUrl: './process-dtl-users.component.html',
  styleUrls: ['./process-dtl-users.component.scss']
})
export class ProcessDtlUsersComponent implements OnInit {
  @Input() isVisible: boolean;

  selectedUser;
  users = [];
  processUsers = [];
  columns = [{ field: 'name', header: 'name' }, { field: 'sso', header: 'SSO' }];
  processId;
  constructor(
    private userSvc: UserService,
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.processId !== 0) {
      this.getUsers();
      this.getProcessUsers();
    }
  }

  addUser() {
    this.processSvc.newProcessUser(this.processId, this.selectedUser.id).subscribe(value => {
      this.msgSvc.add({
        severity: 'success',
        summary: 'New user!',
        detail: `User '${value.user.name}' was added`
      });
      this.selectedUser = null;
      value.name = value.user.name;
      value.sso = value.user.sso;
      value.id = value.user.id;
      this.processUsers.push(value);
    });
  }

  getUsers() {
    this.userSvc.getUsers().subscribe(value => {
      this.users = value;
    });
  }

  getProcessUsers() {
    this.processSvc.getProcessUsers(this.processId).subscribe(value => {
      this.processUsers = value;
      this.processUsers.map(u => {
        u.name = u.user.name;
        u.sso = u.user.sso;
        u.id = u.user.id;
      });
    });
  }

  onDeleteRecord(id) {
    this.processSvc.deleteProcessUser(this.processId, id).subscribe(value => {
      this.processUsers = this.processUsers.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `User was removed from process`
      });
    });
  }
}
