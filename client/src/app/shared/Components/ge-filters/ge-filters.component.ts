import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { GEFiltersService } from './ge-filters.service';
import { GEChipFilter } from 'src/app/modules/dashboard/dashboard-filters/GEChipFilter';

@Component({
  selector: 'app-ge-filters',
  templateUrl: './ge-filters.component.html',
  styleUrls: ['./ge-filters.component.scss'],
  providers: [GEFiltersService]
})
export class GEFiltersComponent implements OnInit {
  @Input() filterSelection = ['from', 'to', 'processes', 'status', 'bu', 'altId', 'adHoc'];
  @Input() displayFilters;
  @Input() childParentRelationship = true;
  @Output() clickFilter = new EventEmitter();
  @Output() onHide = new EventEmitter();
  @Output() onChangeParams = new EventEmitter();
  submissionFilterForm: FormGroup;

  processes = [];
  parentProcesses = [];
  senders = [];
  receivers = [];
  status = [
    { label: 'In Progress', value: 1 },
    { label: 'Success', value: 2 },
    { label: 'Warning', value: 3 },
    { label: 'Failed', value: 4 }
  ];
  businesses = [
    { label: 'Aviation', value: 'AV' },
    { label: 'Corporate', value: 'CO' },
    { label: 'Healthcare', value: 'HC' },
    { label: 'Oil & Gas', value: 'OG' },
    { label: 'Renewables', value: 'RE' },
    { label: 'Total Company', value: 'TC' },
    { label: 'Capital', value: 'CA' },
    { label: 'Power', value: 'PO' },
    { label: 'Industrial', value: 'IN' },
    { label: 'GECSIN', value: 'GN' },
    { label: 'GPINP', value: 'GP' }
  ];

  adHoc = [{ label: 'All', value: null }, { label: 'Scheduled Only', value: 'N' }, { label: 'Adhoc Only', value: 'Y' }];

  get input() {
    return this.submissionFilterForm ? this.submissionFilterForm.getRawValue() : null;
  }

  constructor(
    private fb: FormBuilder,
    private submissionsFilterSvc: GEFiltersService,
    private route: Router,
    private activatedRoute: ActivatedRoute
  ) {
    this.reset();
  }

  ngOnInit() {}

  filter() {
    let chipFilters: GEChipFilter[] = [];

    const queryParams = {
      from: null,
      to: null,
      level: '',
      childId: '',
      parentId: '',
      sender: '',
      receiver: '',
      status: '',
      bu: '',
      altId: '',
      adHoc: null
    };

    // startDate
    queryParams.from = this.submissionFilterForm.value.from
      ? new Date(this.submissionFilterForm.value.from).toISOString().split('T')[0]
      : null;

    // endDate
    queryParams.to = this.submissionFilterForm.value.to
      ? new Date(this.submissionFilterForm.value.to).toISOString().split('T')[0]
      : null;

    queryParams.level = this.submissionFilterForm.value.level;
    // level: process, parent, sender/receiver
    switch (this.submissionFilterForm.value.level) {
      case 'PR': // childId, parentId
        this.submissionFilterForm.value.processes.forEach(pr => {
          chipFilters.push({ id: pr.id, name: pr.name, paramName: 'childId' });
          queryParams.childId += pr.id + ',';
          queryParams.parentId += pr.processParent ? pr.processParent.id + ',' : '';
        });

        queryParams.childId = queryParams.childId === '' ? null : queryParams.childId;
        if (this.childParentRelationship) {
          queryParams.parentId =
            queryParams.parentId === '' ? (queryParams.childId === null ? null : '0') : queryParams.parentId;
        } else {
          queryParams.parentId = null;
        }

        break;

      case 'PA': // parentId, childId
        this.submissionFilterForm.value.parents.forEach(pa => {
          queryParams.parentId += pa.id + ',';
          chipFilters.push({ id: pa.id, name: pa.name, paramName: 'parentId' });
          this.processes.forEach(pr => {
            if (pr.processParent && pr.processParent.id === pa.id) {
              queryParams.childId += pr.id + ',';
            }
          });
        });

        queryParams.parentId = queryParams.parentId === '' ? null : queryParams.parentId;
        if (this.childParentRelationship) {
          queryParams.childId =
            queryParams.childId === '' ? (queryParams.parentId === null ? null : '0') : queryParams.childId;
        } else {
          queryParams.childId = null;
        }

        break;

      case 'SR': // sender, receiver
        this.submissionFilterForm.value.senders.forEach(s => {
          queryParams.sender += s.id + ',';
          chipFilters.push({ id: s.id, name: s.name, paramName: 'sender' });
        });
        this.submissionFilterForm.value.receivers.forEach(r => {
          queryParams.receiver += r.id + ',';
          chipFilters.push({ id: r.id, name: r.name, paramName: 'receiver' });
        });

        queryParams.childId = null;
        queryParams.parentId = null;
        break;
    }

    queryParams.sender = queryParams.sender === '' ? null : queryParams.sender;
    queryParams.receiver = queryParams.receiver === '' ? null : queryParams.receiver;

    // status
    this.submissionFilterForm.value.status.forEach(s => {
      queryParams.status += s + ',';
    });
    queryParams.status = queryParams.status === '' ? null : queryParams.status;

    // bu
    queryParams.bu = this.submissionFilterForm.value.bu;
    if (queryParams.bu) chipFilters.push({ id: queryParams.bu, name: 'Biz: ' + queryParams.bu, paramName: 'bu' });

    // altId
    queryParams.altId = this.submissionFilterForm.value.altId;

    // adHoc
    queryParams.adHoc = this.submissionFilterForm.value.adHoc;
    if (queryParams.adHoc)
      chipFilters.push({ id: queryParams.adHoc, name: 'AdHoc: ' + queryParams.adHoc, paramName: 'adHoc' });

    setTimeout(() => {
      this.displayFilters = false;
    }, 1000);

    localStorage.setItem('submission-chip-filters', JSON.stringify(chipFilters));
    this.onChangeParams.emit(chipFilters);

    this.route.navigate([], {
      queryParams: queryParams,
      relativeTo: this.activatedRoute,
      queryParamsHandling: 'merge'
    });
  }

  onClickClear() {
    this.submissionFilterForm.setValue({
      from: null,
      to: null,
      level: 'PR',
      processes: [],
      parents: [],
      receivers: [],
      senders: [],
      status: [],
      bu: null,
      altId: null,
      adHoc: null
    });
  }

  reset() {
    this.submissionFilterForm = this.fb.group({
      from: null,
      to: null,
      level: 'PR',
      processes: [{ value: [] }],
      parents: [{ value: [], disabled: true }],
      receivers: [{ value: [], disabled: true }],
      senders: [{ value: [], disabled: true }],
      status: [{ value: [] }],
      bu: null,
      altId: null,
      adHoc: null
    });
    this.filterLevelRules();
    this.filtersSetup();
  }

  filterLevelRules() {
    this.submissionFilterForm.get('level').valueChanges.subscribe(value => {
      switch (value) {
        case 'PR':
          this.submissionFilterForm.get('processes').enable();
          this.submissionFilterForm.get('parents').disable();
          this.submissionFilterForm.get('receivers').disable();
          this.submissionFilterForm.get('senders').disable();
          break;
        case 'PA':
          this.submissionFilterForm.get('processes').disable();
          this.submissionFilterForm.get('parents').enable();
          this.submissionFilterForm.get('receivers').disable();
          this.submissionFilterForm.get('senders').disable();
          break;
        case 'SR':
          this.submissionFilterForm.get('processes').disable();
          this.submissionFilterForm.get('parents').disable();
          this.submissionFilterForm.get('receivers').enable();
          this.submissionFilterForm.get('senders').enable();
          break;
        default:
          break;
      }
    });
  }

  filtersSetup() {
    this.submissionsFilterSvc.getAllSenders().subscribe(value => {
      this.senders = value;
    });
    this.submissionsFilterSvc.getAllReceiver().subscribe(value => {
      this.receivers = value;
    });
    this.submissionsFilterSvc.getAllProcesses(true).subscribe((value: any[]) => {
      this.parentProcesses = value.filter(x => x.isParent);
      this.processes = value.filter(x => !x.isParent);
    });
  }

  showFilter(filterName: string): boolean {
    return this.filterSelection.includes(filterName);
  }

  onHideSidebar(event) {
    this.onHide.emit(true);
  }

  /**
   * Before displaying filter panel, update form value with query params.
   * @param event
   */
  onShowSidebar(event) {
    let queryParams = this.activatedRoute.snapshot.queryParams;
    let processes = [];
    let parents = [];
    let receivers = [];
    let senders = [];
    let status = [];

    if (queryParams.childId) {
      processes = queryParams.childId.split(',').map((p: string) => {
        const process = this.processes.find(x => x.id === Number.parseInt(p));
        return process;
      });
      processes.pop();
    }
    if (queryParams.parentId) {
      parents = queryParams.parentId.split(',').map((p: string) => {
        const parent = this.parentProcesses.find(x => x.id === Number.parseInt(p));
        return parent;
      });
      parents.pop();
    }
    if (queryParams.receiver) {
      receivers = queryParams.receiver.split(',').map((p: string) => {
        const receiver = this.receivers.find(x => x.id === Number.parseInt(p));
        return receiver;
      });
      receivers.pop();
    }
    if (queryParams.sender) {
      senders = queryParams.sender.split(',').map((p: string) => {
        const sender = this.senders.find(x => x.id === Number.parseInt(p));
        return sender;
      });
      senders.pop();
    }
    if (queryParams.status) {
      status = queryParams.status.split(',').map((p: string) => {
        const status = this.status.find(x => x.value === Number.parseInt(p));
        return status;
      });
      status.pop();
    }

    this.submissionFilterForm.setValue({
      from: queryParams.from ? new Date(queryParams.from) : null,
      to: queryParams.to ? new Date(queryParams.to) : null,
      level: queryParams.level ? queryParams.level : 'PR',
      processes: processes ? processes : [],
      parents: parents ? parents : [],
      receivers: receivers ? receivers : [],
      senders: senders ? senders : [],
      status: status ? status : [],
      bu: queryParams.bu ? queryParams.bu : null,
      altId: queryParams.altId ? queryParams.altId : null,
      adHoc: queryParams.adHoc ? queryParams.adHoc : null
    });
  }
}
