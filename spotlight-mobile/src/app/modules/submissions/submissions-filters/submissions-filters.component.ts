import { Component, OnInit, Inject } from '@angular/core';
import { SubmissionsFiltersService } from './submissions-filters.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SubmissionParams } from '../SubmissionParams';

@Component({
  selector: 'ge-submissions-filters',
  templateUrl: './submissions-filters.component.html',
  styleUrls: ['./submissions-filters.component.scss']
})
export class SubmissionsFiltersComponent implements OnInit {
  processes = [];
  parents = [];
  statuses = [
    { name: 'In Progress', id: 1 },
    { name: 'Success', id: 2 },
    { name: 'Warning', id: 3 },
    { name: 'Failed', id: 4 }
  ];
  selectedProcesses = [];
  selectedParents = [];
  selectStatuses = [];

  constructor(
    private submissionsFilterSvc: SubmissionsFiltersService,
    public dialogRef: MatDialogRef<SubmissionsFiltersComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit() {
    this.getProcesses();
  }

  getProcesses() {
    this.submissionsFilterSvc.getAllProcesses().subscribe((value: any[]) => {
      this.processes = value.filter(p => p.isParent === false);
      this.parents = value.filter(p => p.isParent === true);
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClickFilter(): void {
    if (this.selectedParents.length > 0) {
      this.processes.forEach(p => {
        if (p.processParent && this.selectedParents.find(pa => pa.id === p.processParent.id)) {
          this.selectedProcesses.push(p);
        }
      });
    }
    const output: SubmissionParams = {
      processes: this.selectedProcesses,
      parents: this.selectedParents,
      statuses: this.selectStatuses
    };
    this.dialogRef.close(output);
  }
}
