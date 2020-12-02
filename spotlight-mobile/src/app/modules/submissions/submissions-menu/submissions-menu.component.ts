import { Component, OnInit, Inject } from '@angular/core';
import { MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef } from '@angular/material/bottom-sheet';
import { Router } from '@angular/router';

@Component({
  selector: 'ge-submissions-menu',
  templateUrl: './submissions-menu.component.html',
  styleUrls: ['./submissions-menu.component.scss']
})
export class SubmissionsMenuComponent implements OnInit {
  constructor(
    private _bottomSheetRef: MatBottomSheetRef<SubmissionsMenuComponent>,
    @Inject(MAT_BOTTOM_SHEET_DATA) public data: any,
    private router: Router
  ) {}

  ngOnInit() {}

  openSubmissionSteps() {
    this.router.navigate(['submissions/steps/', this.data.id]);
    this._bottomSheetRef.dismiss();
  }
}
