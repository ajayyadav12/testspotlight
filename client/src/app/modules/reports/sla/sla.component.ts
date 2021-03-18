import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SubmissionsService } from '../submissions/submissions.service';
import * as moment from 'moment-timezone';
import { MessageService } from 'primeng/api';
import { SlaW2wDetailsComponent } from './sla-w2w-details/sla-w2w-details.component';
import { SlaTrendComponent } from './sla-trend/sla-trend.component';
import { Colors } from 'src/app/shared/Constants/Colors';
enum SLAViews {
	Trends = 'trends',
	Submissions = 'submissions',
}
@Component({
	selector: 'app-sla',
	templateUrl: './sla.component.html',
	styleUrls: ['./sla.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class SlaComponent implements OnInit {
	@ViewChild('w2wComp') w2wComp: SlaW2wDetailsComponent;
	@ViewChild('trendComp') trendComp: SlaTrendComponent;
	@ViewChild('w2wCompexp') w2wCompexp: SlaW2wDetailsComponent;
	@ViewChild('trendCompexp') trendCompexp: SlaTrendComponent;
	selectedView: SLAViews = null;
	selectedRow;
	views = [
		{ label: 'Submissions', value: SLAViews.Submissions },
		{ label: 'Trends', value: SLAViews.Trends },
	];
	submissionsSLA = [];
	loading = false;
	displayExpandModule = false;
	columns = [
		{ field: 'id', header: 'ID' },
		{ field: 'parentId', header: 'Parent ID' },
		{ field: `processName`, header: 'Process' },
		{ field: 'schedStartTime', header: 'Schedule Start Time' },
		{ field: 'schedEndTime', header: 'Schedule End Time' },
		{ field: 'startTime', header: 'Actual Start Time' },
		{ field: 'endTime', header: 'Actual End Time' },
		{ field: 'tolerance', header: 'Tolerance' },
		{ field: 'elapsedTime', header: 'Run Time' },
		{ field: 'elapsedTotalTime', header: 'Total Time' },
		{ field: 'slacompliant', header: 'SLA Compliant' },
	];

	constructor(
		private sidebarSvc: SidebarService,
		private submissionSvc: SubmissionsService,
		private msgSvc: MessageService
	) {
		this.sidebarSvc.title = 'SLA Dashboard';
	}

	ngOnInit(): void {}

	enableCharts(row) {
		this.selectedView = SLAViews.Submissions;
		setTimeout(() => {
			this.selectedRow = row;
			this.w2wComp.drawChart(row);
		}, 500);
	}

	expandDialogBox() {
		this.displayExpandModule = true;
		setTimeout((_) => {
			const maxBtn: any = document.getElementsByClassName('ui-dialog-titlebar-maximize')[0];
			maxBtn.click();
			if (this.selectedView === SLAViews.Trends) {
				this.trendCompexp.drawChart(this.submissionsSLA);
			} else {
				this.w2wCompexp.drawChart(this.selectedRow);
			}
		}, 50);
	}

	getSubmission(filters: any) {
		this.submissionsSLA = [];
		if (filters === null) {
			this.selectedView = null;
			return;
		}
		this.loading = true;
		this.submissionSvc.getSubmissions(filters).subscribe((value) => {
			if (value.content.length === 0) {
				this.selectedView = null;
				this.msgSvc.add({
					severity: 'info',
					summary: `hmm...`,
					detail: `No records found with this criteria. Try another one!`,
				});
			} else {
				this.selectedView = SLAViews.Trends;
				value.content.forEach((s) => this.submissionSLAMapping(s));
				this.submissionsSLA = value.content;
				setTimeout(() => {
					this.trendComp.drawChart(this.submissionsSLA);
				}, 500);
			}
			this.loading = false;
		});
	}

	submissionSLAMapping(submissionSLA) {
		submissionSLA.processName = submissionSLA.process.name;
		submissionSLA.startTime = moment(submissionSLA.startTime).tz('America/New_York').format('MM/DD/YY hh:mm a');

		if (submissionSLA.endTime) {
			submissionSLA.endTime = moment(submissionSLA.endTime).tz('America/New_York').format('MM/DD/YY hh:mm a');
		}
		submissionSLA.tolerance = submissionSLA.scheduledSubmission.scheduleDefinition.tolerance;
		submissionSLA.schedStartTime = moment(submissionSLA.scheduledSubmission.startTime)
			.tz('America/New_York')
			.format('MM/DD/YY hh:mm a');
		submissionSLA.schedEndTime = moment(submissionSLA.scheduledSubmission.endTime)
			.tz('America/New_York')
			.format('MM/DD/YY hh:mm a');
		submissionSLA.columnStyle = {
			background: submissionSLA.slacompliant === 'Success' ? Colors.green : Colors.red,
			padding: '2px 4px',
			'text-align': 'center',
			color: 'white',
			'border-radius': '0.5em',
			'font-size': '16px',
		};
		submissionSLA.steps.sort((a, b) => a.id - b.id);
		submissionSLA.steps.forEach((step) => {
			this.stepMapping(step);
		});
	}

	stepMapping(step) {
		step.startTime = moment(step.startTime).tz('America/New_York').format('MM/DD/YY hh:mm a');
		if (step.endTime) {
			step.endTime = moment(step.endTime).tz('America/New_York').format('MM/DD/YY hh:mm a');
		}
	}
}
