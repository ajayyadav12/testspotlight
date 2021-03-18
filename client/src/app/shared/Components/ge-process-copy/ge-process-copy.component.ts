import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Process } from 'src/app/modules/admin/process/Process';
import { ProcessService } from 'src/app/modules/admin/process/process.service';

@Component({
    selector: 'app-ge-process-copy',
    templateUrl: './ge-process-copy.component.html',
    styleUrls: ['./ge-process-copy.component.scss']
})
export class GeProcessCopyComponent {

    @Input() display = false;
    @Output() displayChange = new EventEmitter<boolean>();
    @Input() process: Process;
    @Output() onCancel = new EventEmitter();

    name = '';
    steps = false;
    notifications = false;
    users = false;
    schedules = false;

    get headerLabel() {
        return `Copy ${this.process ? this.process.name : ''}`;
    }

    constructor(private processService: ProcessService) { }

    onDisplay($event) {
        this.display = $event;
        this.displayChange.emit($event);
    }

    onCopy(): void {
        const settings = [];
        if (this.steps) { settings.push('steps'); }
        if (this.notifications) { settings.push('notifications'); }
        if (this.users) { settings.push('users'); }
        if (this.schedules) { settings.push('schedules'); }
        this.processService.copyProcess(this.process.id, this.name, settings).subscribe(newProcess => {
            location.assign(`process/${newProcess.id}`);
        });
    }

}
