import { Component, OnInit, ViewEncapsulation, Input, Output } from '@angular/core';
import { ProcessService } from '../../process.service';
import { ActivatedRoute } from '@angular/router';
import { TreeNode } from 'primeng/api';
import { EventEmitter } from 'protractor';

@Component({
  selector: 'app-process-dtl-chart',
  templateUrl: './process-dtl-chart.component.html',
  styleUrls: ['./process-dtl-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ProcessDtlChartComponent implements OnInit {
  @Input() incomingProcess;

  ready = false;

  processId;
  process: TreeNode[];

  constructor(private processSvc: ProcessService, private route: ActivatedRoute) {}

  ngOnInit() {
    if (this.incomingProcess) {
      this.process = [
        {
          label: this.incomingProcess.processParent.name,
          expanded: true,
          type: 'process',
          data: { pType: this.incomingProcess.processType },
          children: []
        }
      ];
      this.getChildren(this.incomingProcess.processParent.id);
    } else {
      this.processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);

      if (this.processId !== 0) {
        this.processSvc.getProcess(this.processId).subscribe((process: any) => {
          if (process.isParent) {
            this.process = [
              {
                label: process.name,
                expanded: true,
                type: 'process',
                data: { pType: process.processType },
                children: []
              }
            ];
            this.getChildren(this.processId);
          } else if (process.processParent) {
            this.process = [
              {
                label: process.processParent.name,
                expanded: true,
                type: 'process',
                data: { pType: process.processType },
                children: []
              }
            ];
            this.getChildren(process.processParent.id);
          } else {
            this.process = [
              {
                label: process.name,
                expanded: true,
                type: 'process',
                data: { sender: process.sender, receiver: process.receiver }
              }
            ];
            this.ready = true;
          }
        });
      }
    }
  }

  getChildren(id) {
    this.processSvc.getChildren(id).subscribe((values: any) => {
      const children = [];
      let prev = values.filter(v => {
        return !v.predecessor;
      })[0];
      if (prev) {
        children.push({
          label: prev.name,
          type: 'process',
          data: {
            sender: prev.sender,
            receiver: prev.receiver,
            current: this.incomingProcess
              ? prev.id === this.incomingProcess.id
              : prev.id === this.processId
              ? true
              : false
          }
        });
        let next;
        while (true) {
          next = values.filter(v => {
            return v.predecessor && v.id !== prev.id && v.predecessor.id === prev.id;
          })[0];
          if (next) {
            children.push({
              label: next.name,
              type: 'process',
              data: {
                sender: next.sender,
                receiver: next.receiver,
                current: this.incomingProcess
                  ? next.id === this.incomingProcess.id
                  : next.id === this.processId
                  ? true
                  : false
              }
            });
            prev = next;
          } else {
            break;
          }
        }
      }
      this.process[0].children = children;
      this.ready = true;
    });
  }
}
