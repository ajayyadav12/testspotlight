import { SubmissionCommon } from './../../../../shared/SubmissionCommon';
import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SubmissionsService } from '../submissions.service';
import { Subject } from 'rxjs';
import { DateCommon } from 'src/app/shared/DateCommon';
import { Table } from 'primeng/table';
import * as moment from 'moment-timezone';
import { first, map } from 'rxjs/operators';

@Component({
  selector: 'app-submissions-parents',
  templateUrl: './submissions-parents.component.html',
  styleUrls: ['./submissions-parents.component.scss']
})
export class SubmissionsParentsComponent implements OnInit, OnDestroy {
  @Input() clickRefresh: Subject<any>;
  @Input() autoRefreshOn: boolean;
  autoRefreshHandler;

  @Output() changeView = new EventEmitter();
  @ViewChild('dt', { static: true }) table: Table;

  totalRecords;
  queryParams;
  parents = [];
  displayPopup = { value: false, submission: null };
  loading = false;

  isolatedSubmission;
  showIsolation = false;

  get curPage() {
    return this.table ? this.table.first / 10 : 0;
  }

  get newChildProcess() {
    return {
      info: {
        process: null,
        stats: {
          count: 0,
          totalRecords: 0,
          time: {
            start: null,
            end: null,
            complete: true
          },
          data: []
        },
        labels: {
          name: null,
          elapsedTime: ''
        }
      },
      success: {
        records: 0,
        data: [],
        labels: {
          name: 'Success'
        }
      },
      warning: {
        records: 0,
        data: [],
        labels: {
          name: 'Warning'
        }
      },
      failed: {
        records: 0,
        data: [],
        labels: {
          name: 'Failed'
        }
      },
      progress: {
        records: 0,
        data: [],
        labels: {
          name: 'Progress'
        }
      }
    };
  }

  constructor(private submissionsSvc: SubmissionsService, private route: ActivatedRoute) {
    this.route.queryParams.subscribe(params => {
      if (params === {} && localStorage.getItem('submission-view') !== null) {
        return;
      }
      this.queryParams = params;
      if (this.table) { this.table.reset(); }
      this.getParentSubmissions(params);
    });
  }

  ngOnInit() {
    if (this.clickRefresh) {
      this.clickRefresh.subscribe(params => {
        this.getParentSubmissions(params);
      });
    }

    this.autoRefreshHandler = setInterval(() => {
      if (this.autoRefreshOn) {
        this.getParentSubmissions(this.queryParams);
      }
    }, 1000 * 60);
  }

  ngOnDestroy() {
    clearInterval(this.autoRefreshHandler);
    this.clickRefresh.unsubscribe();
  }

  /**
   * Retrives data for Parent Submission table
   * and maps it for the row dropdown (children component)
   * @param params Query params
   */
  getParentSubmissions(params?) {
    this.loading = true;
    const _params: any = {};
    Object.assign(_params, params ? params : this.queryParams);
    _params.sortOrder = -1;
    _params.sortField = 'id';
    _params.size = 10;
    _params.page = this.curPage;

    this.submissionsSvc.getParentSubmissions(_params).subscribe(
      (value: any) => {
        this.parents = [];
        this.parents = value.content;
        this.totalRecords = value.totalElements;
        this.parents.map(s => {
          this.parentMapping(s);
        });
        this.loading = false;
      },
      err => {
        this.loading = false;
      }
    );
    this.displayPopup.value = false;
    this.displayPopup.submission = null;
  }

  exit(child) {
    this.changeView.emit({ element: child });
  }

  parentMapping(s) {
    // this.childTotalRecords = 0;
    moment.tz.setDefault('America/New_York');
    let startMoment = moment(s.startTime);
    let endMoment = moment(s.endTime);
    s.title = s.process.name;
    s.status = s.status.name;
    s.start = new Date(s.startTime);
    s.end = new Date(s.endTime);
    s.elapsedTime = DateCommon.dateDifference(s.startTime, s.endTime ? s.endTime : new Date(), true);

    s.startTime = startMoment.tz('America/New_York').format('MM/DD/YY hh:mm a');
    if (s.endTime) {
      s.endTime = endMoment.tz('America/New_York').format('MM/DD/YY hh:mm a');
    }


    // prepare empty mapping for child processes
    s.childProcesses = [];
    if (s.children.length > 0) {
      s.children.forEach(map => {
        s.records += map.records;
        s.errors += map.errors;
        s.warnings += map.warnings;
      })

    }

    // sort children before mapping so that data[] in process map (and its status maps) are also sorted
    if (s.children.length > 1) {
      s.children.sort((a, b) => {
        // future: sort by predecessor/successor
        return a.id - b.id;
      });
    }

    // map children and create child process maps
    s.children.map(c => {
      this.childMapping(c, s);
    });

    if (s.childProcesses.length > 1) {
      s.childProcesses.sort((a, b) => {
        return a.info.stats.data[0].id - b.info.stats.data[0].id;
      });
    }
  }

  childMapping(c, s) {
    c.title = c.process.name;
    c.status = c.status.name;
    c.color = this.submissionStatusColor(c.status);
    c.parentId = s.id;
    c.start = new Date(c.startTime);
    c.end = new Date(c.endTime);
    c.elapsedTime = DateCommon.dateDifference(c.startTime, c.endTime ? c.endTime : new Date(), true);

    // place submission into correct child process map
    let found = false;
    let index = 0;

    // if process map already created, add submission
    s.childProcesses.forEach(map => {
      if (map.info.process.id === c.process.id) {
        found = true;

        s.childProcesses[index].info.stats.count++;
        s.childProcesses[index].info.stats.totalRecords += c.records;


        // update time window for map
        if (!c.endTime) {
          s.childProcesses[index].info.stats.time.complete = false;
        } else if (c.end > s.childProcesses[index].info.stats.time.end) {
          s.childProcesses[index].info.stats.time.end = new Date(c.end);
        }
        if (c.start < s.childProcesses[index].info.stats.time.start) {
          s.childProcesses[index].info.stats.time.start = new Date(c.start);
        }
        s.childProcesses[index].info.labels.elapsedTime = DateCommon.dateDifference(
          s.childProcesses[index].info.stats.time.start,
          s.childProcesses[index].info.stats.time.complete ? s.childProcesses[index].info.stats.time.end : new Date(),
          true
        );

        s.childProcesses[index].info.stats.data.push(c);

        // map by status
        if (c.status === 'success') {
          s.childProcesses[index].success.data.push(c);
          s.childProcesses[index].success.records += c.records;
        } else if (c.status === 'warning') {
          s.childProcesses[index].warning.data.push(c);
          s.childProcesses[index].warning.records += c.records;
        } else if (c.status === 'failed') {
          s.childProcesses[index].failed.data.push(c);
          s.childProcesses[index].failed.records += c.records;
        } else {
          s.childProcesses[index].progress.data.push(c);
          s.childProcesses[index].progress.records += c.records;
        }

        return;
      }

      index++;
    });

    // if process map does not yet exist, create new one based on child submission
    if (!found) {
      s.childProcesses.push(this.newChildProcess);
      index = s.childProcesses.length - 1;

      s.childProcesses[index].info.stats.count++;
      s.childProcesses[index].info.stats.totalRecords += c.records;

      // general process map info and time window
      s.childProcesses[index].info.process = c.process;
      s.childProcesses[index].info.labels.name = c.process.name;
      s.childProcesses[index].info.stats.time.start = new Date(c.startTime);
      s.childProcesses[index].info.stats.time.end = new Date(c.endTime);
      if (!c.endTime) {
        s.childProcesses[index].info.stats.time.complete = false;
      }
      s.childProcesses[index].info.labels.elapsedTime = DateCommon.dateDifference(
        s.childProcesses[index].info.stats.time.start,
        c.endTime ? s.childProcesses[index].info.stats.time.end : new Date(),
        true
      );

      s.childProcesses[index].info.stats.data.push(c);

      // map process by status
      if (c.status === 'success') {
        s.childProcesses[index].success.data.push(c);
        s.childProcesses[index].success.records += c.records;
      } else if (c.status === 'warning') {
        s.childProcesses[index].warning.data.push(c);
        s.childProcesses[index].warning.records += c.records;
      } else if (c.status === 'failed') {
        s.childProcesses[index].failed.data.push(c);
        s.childProcesses[index].failed.records += c.records;
      } else {
        s.childProcesses[index].progress.data.push(c);
        s.childProcesses[index].progress.records += c.records;
      }
    }
  }

  openIsolationMode(submission) {
    this.isolatedSubmission = submission;
    submission.steps = [];
    submission.notes = '';
    submission.children.forEach(child => {
      child.steps.forEach(step => {
        step.parentChildProcessName = child.process.name + ' > ';
        step.notes = step.notes ? step.notes : '';
        if (child.altId) {
          step.notes = `<i>${child.altId}</i> | ${step.notes}`;
        }
      });
      submission.steps.push(...child.steps);
      const altIdSection = child.altId ? ` <i>(${child.altId})</i>` : '';
      submission.notes =
        `<b>#${child.id} ${child.process.name}</b>${altIdSection} - Status: <b>${child.status}</b> <br> ${
        child.notes ? child.notes : ''
        } <br><br>` + submission.notes;
    });
    this.showIsolation = true;
  }

  submissionStatusColor(status) {
    return SubmissionCommon.submissionStatusColor(status);
  }
}
