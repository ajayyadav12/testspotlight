import { Component, OnInit } from '@angular/core';
import { SessionService } from 'src/app/core/services/session.service';
import { ActivatedRoute, Router } from '@angular/router';
import { SubmissionsService } from '../submissions.service';

@Component({
  selector: 'ge-submissions-steps',
  templateUrl: './submissions-steps.component.html',
  styleUrls: ['./submissions-steps.component.scss']
})
export class SubmissionsStepsComponent implements OnInit {
  id: number;
  steps = [];
  constructor(
    private sessionSvc: SessionService,
    private submissionsSvc: SubmissionsService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.id = Number.parseInt(params['id']);
      this.sessionSvc.title = `Submission ${this.id} Steps`;
      this.getSubmissionSteps();
    });
  }

  getSubmissionSteps() {
    this.submissionsSvc.getSubmissionSteps(this.id).subscribe(value => {
      value.sort((x, y) => y.id - x.id);
      this.steps = value;
    });
  }

  onClickBackBtn() {
    this.router.navigate(['/submissions']);
  }

  stepStatusColor(status) {
    return this.submissionsSvc.submissionStatusColor(status);
  }
}
