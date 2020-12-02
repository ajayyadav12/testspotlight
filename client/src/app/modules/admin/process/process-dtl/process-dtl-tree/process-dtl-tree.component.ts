import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { ProcessService } from '../../process.service';
import { ActivatedRoute } from '@angular/router';
import { Menu } from 'primeng/menu';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-process-dtl-tree',
  templateUrl: './process-dtl-tree.component.html',
  styleUrls: ['./process-dtl-tree.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ProcessDtlTreeComponent implements OnInit {
  processes = [];
  processTree = [];
  selectedNode;
  selectedProcess;
  processId: number;
  isLoaded = false;
  parentChildren = [];

  get maxSeq(): number {
    return this.parentChildren.length ? this.parentChildren[this.parentChildren.length - 1].seq : 0;
  }

  @ViewChild('menu', { static: true }) menu: Menu;
  items = [
    {
      label: 'Add Child',
      command: _ => {
        this.addChild();
      }
    },
    {
      label: 'Remove',
      command: _ => {
        this.removeChild();
      }
    }
  ];

  constructor(private processSvc: ProcessService, private route: ActivatedRoute, private sidebarSvc: SidebarService) {}

  ngOnInit() {
    this.getProcesses();
    this.processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.processId !== 0) {
      this.getChildren();
    }
  }

  addChild() {
    if (!this.selectedProcess) {
      alert('Please select a process');
      return;
    }

    const processChild = {
      id: 0,
      process: { id: this.processId },
      processChild: { id: this.selectedProcess.id },
      seq: this.selectedNode.seq === 0 ? this.maxSeq + 10 : this.selectedNode.seq
    };
    this.processSvc.updateProcessMap(this.processId, processChild).subscribe(value => {
      this.getChildren();
      this.selectedProcess = null;
    });
  }

  currentProcessColor(nodeLabel) {
    if (nodeLabel === this.sidebarSvc.title.split('Process: ')[1]) return '#005fb8c9';
    else return null;
  }

  getChildren() {
    this.isLoaded = false;
    this.processTree = [];

    this.processSvc.getProcessChildren(this.processId).subscribe(value => {
      let parentNode = {
        seq: 0,
        label: this.sidebarSvc.title.split('Process: ')[1],
        childId: this.processId,
        expanded: true,
        children: []
      };

      if (!value.length) {
        this.isLoaded = true;
        this.processTree.push(parentNode);
        return;
      }

      // Change first node label and disable menu if child process
      if (parentNode.label !== value[0].process.name) {
        parentNode.label = value[0].process.name;
        this.items = [];
      }

      this.processTree.push(parentNode);
      this.parentChildren = value;
      let latestSeq = 0;
      const firstNode = this.processTree[0];
      let latestNode = this.processTree[0];
      for (let index = 0; index < value.length; index++) {
        const element = value[index];
        if (latestSeq === element.seq) {
          latestNode.children.push({
            id: element.id,
            seq: element.seq,
            label: element.processChild.name,
            childId: element.processChild.id,
            senderName: element.processChild.senderName,
            receiverName: element.processChild.receiverName,
            expanded: true,
            children: []
          });
          latestNode = latestNode.children[0];
        } else {
          firstNode.children.push({
            id: element.id,
            seq: element.seq,
            label: element.processChild.name,
            childId: element.processChild.id,
            senderName: element.processChild.senderName,
            receiverName: element.processChild.receiverName,
            expanded: true,
            children: []
          });
          latestNode = firstNode.children[firstNode.children.length - 1];
        }
        latestSeq = element.seq;
      }
      this.isLoaded = true;
    });
  }

  getProcesses() {
    this.processSvc.getAllProcesses(true).subscribe(value => {
      this.processes = value;
    });
  }

  removeChild() {
    if (!this.selectedNode.id) {
      alert('Cannot delete parent node');
      return;
    }
    this.processSvc.deleteProcessMap(this.processId, this.selectedNode.id).subscribe(value => {
      this.getChildren();
      this.selectedProcess = null;
    });
  }

  onNodeSelect(event) {
    this.menu.toggle(event.originalEvent);
    this.selectedNode = event.node;
  }
}
