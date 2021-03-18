import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ProcessService } from '../admin/process/process.service';
import { SystemService } from '../admin/system/system.service';
import { SubmissionsService } from '../reports/submissions/submissions.service';
import { MessageService } from 'primeng/api';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.scss']
})
export class UploadComponent implements OnInit {

    form: FormGroup;
    systems = [];
    processes = [];
    files = [];
    loading = false;

    constructor(private sidebarService: SidebarService,
        private formBuilder: FormBuilder,
        private processService: ProcessService,
        private systemService: SystemService,
        private submissionService: SubmissionsService,
        private messageService: MessageService) { }



    ngOnInit(): void {
        this.sidebarService.title = 'Upload';
        this.form = this.formBuilder.group({
            name: ['', Validators.required],
            comments: [''],
            process: [null, Validators.required],
            sender: [null, Validators.required],
            receiver: [null, Validators.required]
        });
    }

    getProcesses() {
        this.processService.getProcessSubmitPermission().subscribe(processes => {
            this.processes = processes;
        });
    }

    getSystems() {
        this.systemService.getAllSystems().subscribe(systems => {
            this.systems = systems;
        });
    }

    onUpload(event, uploader) {
        this.loading = true;
        const formData = new FormData();
        formData.append('name', this.form.value.name || '');
        formData.append('comments', this.form.value.comments || '');
        if (this.form.value.process) {
            formData.append('processId', this.form.value.process.id);
        }
        if (this.form.value.sender) {
            formData.append('senderId', this.form.value.sender.id);
        }
        if (this.form.value.receiver) {
            formData.append('receiverId', this.form.value.receiver.id);
        }
        event.files.forEach(file => {
            formData.append('files', file);
        });
        this.submissionService.uploadSubmissionFile(formData).subscribe(res => {
            var recordsCount = res.length;
            for (var i = 0; i < res.length; i++) {
                if (res[i]["fileValidationError"] != null && res[i]["fileValidationError"] != '') {
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error ocurred-',
                        detail: res[i]["fileValidationError"] + ' for ' + res[i]["fileName"],
                        life: 10000
                    });

                    recordsCount = recordsCount - 1;
                }
            }


            if (recordsCount > 0) {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Files uploaded',
                    detail: `${recordsCount} submissions created`,
                    life: 10000
                });
            }
            this.form.reset();
            uploader.clear();
            this.loading = false;
        }, (err: HttpErrorResponse) => {
            this.messageService.add({
                severity: 'error',
                summary: 'An error ocurred',
                detail: err.message
            });
        });
    }

}