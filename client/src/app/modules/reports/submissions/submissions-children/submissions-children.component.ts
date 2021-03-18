import { Component, OnInit, Input, Output, ViewEncapsulation, EventEmitter } from '@angular/core';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-submissions-children',
  templateUrl: './submissions-children.component.html',
  styleUrls: ['./submissions-children.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SubmissionsChildrenComponent implements OnInit {
  @Input() isLightVersion;
  @Input() parent;
  @Input() customGroup;
  @Input() showGrouped;
  @Output() changeView = new EventEmitter();

  responsiveOptions = [
    {
      breakpoint: '1500px',
      numVisible: 4,
      numScroll: 4
    },
    {
      breakpoint: '1350px',
      numVisible: 3,
      numScroll: 3
    },
    {
      breakpoint: '1200px',
      numVisible: 2,
      numScroll: 2
    },
    {
      breakpoint: '800px',
      numVisible: 1,
      numScroll: 1
    }
  ];

  displayCustomCarousel = false;
  dataGroup: any = [];

  displayNotesDialog = false;
  notesData: any;

  get processes() {
    return this.parent ? this.parent.childProcesses : null;
  }

  get children() {
    return this.parent ? this.parent.children : null;
  }

  constructor(private msgSvc: MessageService) { }

  ngOnInit() { }

  openNotesDialog(submission) {
    if (!this.isLightVersion) {
      this.notesData = submission.notes ? submission.notes : submission.info.stats.data[0].notes;
      this.displayNotesDialog = true;
    }
  }

  popup(child) {
    if (!this.showGrouped) {
      this.changeView.emit(child);
    } else if (child.info.stats.count === 1) {
      this.changeView.emit(child.info.stats.data[0]);
    } else {
      this.displayCustomCarousel = true;
      this.dataGroup = child.info.stats.data;
    }
  }
}
