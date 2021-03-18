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
  @Input() isVisible: boolean = true;

  selectedUser = [];
  selectedIds = [];
  selectUser = [];
  users = [];
  processUsers = [];
  columns = [{ field: 'name', header: 'name' }, { field: 'sso', header: 'SSO' }];
  processId;
  constructor(
    private userSvc: UserService,
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.parent.params.subscribe(params => {
      this.processId = params['id'] || 0;
      if (this.processId != 0) {
        this.getProcessUsers();
      }
    });
  }

  addUser() {
    if (this.selectedUser !== null) {
      this.selectedUser.forEach((element) => {
        this.selectedIds.push(element.id);
      });
      this.processSvc.newProcessUser(this.processId, this.selectedIds).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'New users added!',
          // detail: `User '${value.user.name}' was added`
        });
        this.selectedUser = null;
        this.selectedIds = [];
        for (let i = 0; i < value.length; i++) {
          value[i].name = value[i].user.name;
          value[i].sso = value[i].user.sso;
          value[i].id = value[i].user.id;
          this.processUsers.push(value[i]);
          //s this.selectUser.push(value[i]);s
        }

      });
      const session = JSON.parse(localStorage.getItem("session"));
      if (session) {
        session.processes.push(this.processId);
        localStorage.setItem("session", JSON.stringify(session));
      }
    }
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
      const session = JSON.parse(localStorage.getItem("session"));
      if (session) {
        session.processes.pop(this.processId);
        localStorage.setItem("session", JSON.stringify(session));
      }
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `User was removed from process`
      });
    });
  }
}
