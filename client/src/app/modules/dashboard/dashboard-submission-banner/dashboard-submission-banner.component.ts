import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';
import * as Chart from 'chart.js';
import { Colors } from 'src/app/shared/Constants/Colors';

@Component({
	selector: 'app-dashboard-submission-banner',
	templateUrl: './dashboard-submission-banner.component.html',
	styleUrls: ['./dashboard-submission-banner.component.scss'],
})
export class DashboardSubmissionBannerComponent implements OnInit, AfterViewInit {
	@Input() uniqueId = '';
	@Output() displayDrillDownDialog = new EventEmitter<boolean>();

	constructor(private submissionSvc: SubmissionsService, private activatedRoute: ActivatedRoute) {}
	days = 7;
	params;
	isShowingLineChart = true;
	warning = 0;
	success = 0;
	inProgressChart;
	failedChart;
	delayedChart;

	ngOnInit() {}

	ngAfterViewInit(): void {
		this.activatedRoute.queryParams.subscribe((params) => {
			this.days = params.days ? params.days : this.days;
			this.getSubmissionCount(params);
		});
		this.drawContent();
	}

	drawContent() {
		Chart.pluginService.register({
			afterDraw: (chart) => {
				if (chart.config.type !== 'doughnut') return;
				const width = chart.width;
				const height = chart.height;
				const ctx = chart.ctx;

				let fontSize = (height / 150).toFixed(2);
				ctx.font = fontSize + 'em sans-serif';
				ctx.textBaseline = 'middle';

				const text = chart.options.title.text[0];
				const value = chart.options.title.text[1];
				const textX = Math.round((width - ctx.measureText(text).width) / 2);
				const textY = height / 2;

				ctx.fillText(text, textX, textY + 15);
				fontSize = (height / 65).toFixed(2);
				ctx.font = fontSize + 'em sans-serif';
				ctx.fillText(value, textX, textY - 10);
				ctx.save();
			},
		});
	}

	getSubmissionCount(params: any = { days: this.days }) {
		this.params = this.getUpdatedParams(params);

		this.submissionSvc.getSubmissionCount(this.params).subscribe((value) => {
			this.drawChartFailed(value);
			this.drawChartInProgress(value);
			this.drawChartInDelayed(value);
			this.drawChartInWarning(value);
			this.drawChartInSuccess(value);
		});
	}

	getUpdatedParams(_params) {
		const params: any = {};
		Object.assign(params, _params);
		switch (params.level) {
			case 'PR':
				params.parentId = null;
				params.receiver = null;
				params.sender = null;
				break;
			case 'PA':
				params.childId = null;
				params.receiver = null;
				params.sender = null;
				break;
			case 'SR':
				params.childId = null;
				params.parentId = null;
				break;
		}

		return {
			days: params.days ? params.days : this.days,
			childId: params.childId ? params.childId : '-1,',
			parentId: params.parentId ? params.parentId : '-1,',
			receiver: params.receiver ? params.receiver : '-1,',
			sender: params.sender ? params.sender : '-1,',
			adHoc: params.adHoc ? params.adHoc : '-1',
			bu: params.bu ? params.bu : '-1',
		};
	}

	drawChartFailed(submissionStats: any[]) {
		const canvas: any = document.getElementById('bonus_chart_failed');

		let failed = 0;
		let unacknowledged = 0;

		submissionStats.forEach((ss) => {
			failed = failed + ss[1];
			unacknowledged = unacknowledged + ss[6];
		});

		if (this.failedChart) this.failedChart.destroy();

		this.failedChart = new Chart(canvas.getContext('2d'), {
			type: 'doughnut',
			data: {
				datasets: [
					{
						data: [failed - unacknowledged, unacknowledged],
						backgroundColor: [Colors.red, Colors.yellow],
					},
				],
				labels: ['Acknowledge', 'Unacknowledged'],
			},
			options: {
				cutoutPercentage: 80,
				responsive: false,
				legend: {
					display: false,
				},
				title: {
					text: ['Failed', failed.toString()],
				},
				onClick: (event, activeElements) => {
					this.showDialog(1);
				},
			},
		});
	}

	drawChartInProgress(submissionStats: any[]) {
		const canvas: any = document.getElementById('bonus_chart_inprogress');

		let inprogress = 0;
		let longrunning = 0;

		submissionStats.forEach((ss) => {
			inprogress = inprogress + ss[7];
			longrunning = longrunning + ss[3];
		});

		if (this.inProgressChart) this.inProgressChart.destroy();

		this.inProgressChart = new Chart(canvas.getContext('2d'), {
			type: 'doughnut',
			data: {
				datasets: [
					{
						data: [Math.max(inprogress - longrunning, 0), longrunning],
						backgroundColor: [Colors.blue, Colors.gray],
					},
				],
				labels: ['In Progress', 'Long Running'],
			},
			options: {
				cutoutPercentage: 80,
				responsive: false,
				legend: {
					display: false,
				},
				title: {
					text: ['In Progress', Math.abs(inprogress - longrunning).toString()],
				},
				onClick: (event, activeElements) => {
					this.showDialog(2);
				},
			},
		});
	}

	drawChartInDelayed(submissionStats: any[]) {
		const canvas: any = document.getElementById('bonus_chart_delayed');
		let delayed = 0;
		let unacknowledged = 0;

		submissionStats.forEach((ss) => {
			delayed = delayed + ss[4];
			unacknowledged = unacknowledged + ss[8];
		});

		if (this.delayedChart) this.delayedChart.destroy();

		this.delayedChart = new Chart(canvas.getContext('2d'), {
			type: 'doughnut',
			data: {
				datasets: [
					{
						data: [delayed, unacknowledged],
						backgroundColor: [Colors.gray, '#F17D1B'],
					},
				],
				labels: ['Delayed', 'Unacknowledged'],
			},
			options: {
				cutoutPercentage: 80,
				responsive: false,
				legend: {
					display: false,
				},
				title: {
					text: ['Delayed', delayed.toString()],
				},
				onClick: (event, activeElements) => {
					this.showDialog(3);
				},
			},
		});
	}

	drawChartInWarning(submissionStats: any[]) {
		let warning = 0;

		submissionStats.forEach((ss) => {
			warning = warning + ss[2];
		});

		this.warning = warning;
	}

	drawChartInSuccess(submissionStats: any[]) {
		let success = 0;

		submissionStats.forEach((ss) => {
			success = success + ss[5];
		});

		this.success = success;
	}

	showDialog(value) {
		this.displayDrillDownDialog.emit(value);
	}

	changeMousePointerOn() {
		document.body.style.cursor = 'pointer';
	}

	changeMousePointerOut() {
		document.body.style.cursor = 'initial';
	}
}
