import { Component, OnInit, Input } from '@angular/core';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-process-dtl-steps',
  templateUrl: './process-dtl-steps.component.html',
  styleUrls: ['./process-dtl-steps.component.scss']
})
export class ProcessDtlStepsComponent implements OnInit {
  @Input() isVisible: boolean;

  stepName = '';
  required = false;
  parallel = false;
  processSteps = [];
  columns = [
    { field: 'name', header: 'name' },
    { field: 'required', header: 'required' },
    { field: 'duration', header: 'duration (mins)' },
    { field: 'parallel', header: 'parallel' }
  ];
  processId;
  constructor(private processSvc: ProcessService, private msgSvc: MessageService, private route: ActivatedRoute) {}

  ngOnInit() {
    this.processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.processId !== 0) {
      this.getProcessSteps(this.processId);
    }
  }

  addStep() {
    this.processSvc.newProcessStep(this.processId, this.stepName, this.required, this.parallel).subscribe(value => {
      this.msgSvc.add({
        severity: 'success',
        summary: 'New step in town!',
        detail: `Step '${value.name}' added`
      });
      this.stepName = '';
      this.required = false;
      this.parallel = false;
      this.getProcessSteps(this.processId);
    });
  }

  getProcessSteps(id: number) {
    this.processSvc.getAllProcessSteps(id).subscribe((value: any[]) => {
      const endStep = value.splice(1, 1);
      value.push(endStep[0]);
      this.processSteps = value;
    });
  }

  onDeleteRecord(id) {
    this.processSvc.deleteProcessStep(this.processId, id).subscribe(value => {
      this.processSteps = this.processSteps.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Step was removed from process`
      });
    });
  }
}
