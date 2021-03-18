import { Component, OnInit, Input, EventEmitter } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SubmissionsService } from '../reports/submissions/submissions.service';
import { GENotes } from 'src/app/shared/Components/GENotes';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  providers: [SubmissionsService]
})
export class DashboardComponent implements OnInit {
  displayDrillDownDialog: boolean;
  drillDownData: GENotes;
  displayDisabledDialog = false;
  displayTypeDrillDown = 3;
  types=[{label: 'Ops Dashboard', value: 0}, {label: 'Summary', value: 1}];
  displayType = 0;

  columns = [
    { header: 'ID', field: 'id', width: '4%' },
    { header: 'Process', field: 'processName', width: '12%' },
    { header: 'Planned Start', field: 'start', width: '9%' },
    { header: 'Planned End', field: 'end', width: '9%' }
  ];
  now = new Date();
  sevenDaysInThePast = new Date(new Date().setDate(new Date().getDate() - 7));
  displayExpandModule = false;
  expandedModule = '';

  get defaultParams(): any {
    return {
      from: this.sevenDaysInThePast.toISOString().split('T')[0],
      to: this.now.toISOString().split('T')[0],
      page: 0,
      size: 10000,
      sortField: 'id',
      sortOrder: -1
    };
  }

  constructor(private sidebarSvc: SidebarService) {}

  @Input() getSubmissions = new EventEmitter<boolean>();

  ngOnInit() {
    this.sidebarSvc.title = 'Operations Dashboard';
  }

  onDisplayDrillDown(value: number) {
    this.displayDrillDownDialog = true;
    this.displayTypeDrillDown = value;

    this.getSubmissions.emit();
  }

  expandModule(moduleName: string) {
    this.expandedModule = moduleName;
    this.displayExpandModule = true;
    setTimeout(_ => {
      const maxBtn: any = document.getElementsByClassName('ui-dialog-titlebar-maximize')[0];
      maxBtn.click();
    }, 50);
  }
}
