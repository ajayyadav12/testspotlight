import { Component, OnInit } from '@angular/core';
import { SystemService } from '../../../system/system.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SessionService } from 'src/app/core/session/session.service';
import { ProcessService } from '../../../process/process.service';
import { UserService } from '../../user.service';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-user-dtl-permission',
  templateUrl: './user-dtl-permission.component.html'
})
export class UserDtlPermissionComponent implements OnInit {
  systems: any;
  permissionForm: FormGroup;
  canEdit: any;
  loading: any;
  id;

  permissions: any [];

	columns = [
		{ field: 'sender', header: 'Sender' },
		{ field: 'receiver', header: 'Receiver' },
    { field: 'view', header: 'View' },
    { field: 'upload', header: 'Upload' }
	];

  constructor(private systemSvc: SystemService,     
              private fb: FormBuilder,    
              private sessionSvc: SessionService,              
              private userService: UserService,
              private router: Router,
              private route: ActivatedRoute,
              private msgSvc: MessageService) {
                this.setupForm();
                
                this.id = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
                if (this.id !== 0) {
                  this.getPermissions(this.id);
                }
               }

  ngOnInit(): void {
  }

  get isAdmin() {
    return this.sessionSvc.role === 'admin';
  }

  getSenderReceiver() {
    this.systemSvc.getAllSystems().subscribe(value => {
      this.systems = value;
    });
  }

  setupForm() {

    this.permissionForm = this.fb.group({
      view: [null, Validators.required],
      upload: false,
      receiver: [null, Validators.required],
      sender: [null, Validators.required]
    });
    if (!this.isAdmin) {
      this.permissionForm.disable();
    }
  }

  getPermissions(id) {

    this.userService.getUserPermissions(id).subscribe(
      (value: any[]) => {

        this.permissions = [];
        this.loading = true;

        value.forEach(s => { 
          this.permissions.push({
            id: s[6],
            senderId: s[0],
            receiverId: s[1],
            sender: s[2],
            receiver: s[3],
            view: s[4] == 1 ? true : false,
            upload: s[5] == 1 ? true : false,            
          });

      });

      this.loading = false;
    });
}

  grantPermissions() {
    if (this.id !== 0) {
      this.userService.grantPermission(this.id, this.permissionForm.value).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Permissions granted!',
          detail: `User Permissions updated`
        });
        this.getPermissions(this.id)
      });
    } 
  }

  onDeleteRecord(id) {
		this.userService.removePermission(this.id, id).subscribe((value) => {
			this.permissions = this.permissions.filter((p) => {
				return p.id !== id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `Permission was ungranted`
			});
		});
	}
}
