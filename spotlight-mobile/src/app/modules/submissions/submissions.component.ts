import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { SessionService } from 'src/app/core/services/session.service';
import { SubmissionsService } from './submissions.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { SubmissionsMenuComponent } from './submissions-menu/submissions-menu.component';
import { MatDialog } from '@angular/material/dialog';
import { SubmissionsFiltersComponent } from './submissions-filters/submissions-filters.component';
import { SubmissionParams } from './SubmissionParams';

@Component({
  selector: 'ge-submissions',
  templateUrl: './submissions.component.html',
  styleUrls: ['./submissions.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SubmissionsComponent implements OnInit {
  page = 0;
  showSpinner = false;
  submissions = [];
  params;
  chipParams = []; // Label, ID and field
  feedDate = localStorage.getItem('lastDateSelected')
    ? new Date(Number.parseInt(localStorage.getItem('lastDateSelected')))
    : new Date();
  showNoMoreSubmissions = false;

  get defaultParams(): any {
    return {
      from: this.feedDate.toISOString().split('T')[0],
      to: this.feedDate.toISOString().split('T')[0],
      page: this.page,
      size: 10,
      sortField: 'id',
      sortOrder: -1
    };
  }

  constructor(
    private sessionSvc: SessionService,
    private submissionsSvc: SubmissionsService,
    private _snackBar: MatSnackBar,
    private _bottomSheet: MatBottomSheet,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    this.sessionSvc.title = 'Submissions';
    this.getSubmissions();
  }

  getSubmissions(_params = null) {
    this.showSpinner = true;
    const params = !_params ? this.defaultParams : _params;
    this.submissionsSvc.getSubmissions(params).subscribe(value => {
      this.submissions.push(...value.content);
      if (!value.content.length) {
        if (!this.submissions.length) {
          this._snackBar.open('Nothing found. Try with another date!');
        }
        this.showNoMoreSubmissions = true;
      } else {
        this.showNoMoreSubmissions = false;
      }

      this.showSpinner = false;
    });
  }

  getsubmissionFilters(filterResults: SubmissionParams): any {
    const queryParams = {
      from: this.feedDate.toISOString().split('T')[0],
      to: this.feedDate.toISOString().split('T')[0],
      page: this.page,
      size: 10,
      sortField: 'id',
      sortOrder: -1,
      childId: '',
      parentId: '',
      status: ''
    };

    filterResults.processes.forEach(p => {
      queryParams.childId += p.id + ',';
      this.chipParams.push({ label: p.name, field: 'childId', id: p.id });
    });

    filterResults.parents.forEach(p => {
      queryParams.parentId += p.id + ',';
      this.chipParams.push({ label: p.name, field: 'parentId', id: p.id });
    });

    filterResults.statuses.forEach(s => {
      queryParams.status += s.id + ',';
      this.chipParams.push({ label: s.name, field: 'status', id: s.id });
    });

    return queryParams;
  }

  openSubmissionsFilters() {
    const dialogRef = this.dialog.open(SubmissionsFiltersComponent, {
      width: '250px',
      data: { params: [] }
    });

    dialogRef.afterClosed().subscribe((result: SubmissionParams) => {
      if (!result) return;
      // Clean up
      this.chipParams = [];
      this.page = 0;
      this.submissions = [];

      // create params
      this.params = this.getsubmissionFilters(result);

      // Make call with params
      this.getSubmissions(this.params);
    });
  }

  openSubmissionMenu(submission) {
    this._bottomSheet.open(SubmissionsMenuComponent, {
      data: submission
    });
  }

  onScroll() {
    this.page++;
    if (this.params) {
      this.params.page = this.page;
    }

    this.getSubmissions(this.params);
  }

  /**
   * Refresh Settings and get submissions
   */
  onClickManualRefresh() {
    localStorage.setItem('lastDateSelected', this.feedDate.getTime().toString());
    this.page = 0;
    this.submissions = [];
    this.getSubmissions(this.params);
  }

  removeFilter(param) {
    this.chipParams = this.chipParams.filter(p => p !== param);
    switch (param.field) {
      case 'childId':
        this.params.childId = this.params.childId.replace(param.id + ',', '');
      case 'parentId':
        this.params.parentId = this.params.parentId.replace(param.id + ',', '');
      case 'status':
        this.params.status = this.params.status.replace(param.id + ',', '');
        break;

      default:
        break;
    }

    this.page = 0;
    this.params.page = this.page;
    this.submissions = [];

    this.getSubmissions(this.params);
  }

  /**
   * Get status color depending on status.
   * @param status status name
   */
  submissionStatusColor(status) {
    return this.submissionsSvc.submissionStatusColor(status);
  }
}
