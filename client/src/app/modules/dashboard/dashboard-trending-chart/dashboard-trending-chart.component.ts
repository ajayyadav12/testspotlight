import { Component, OnInit, Input, AfterViewInit } from '@angular/core';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';
import * as Chart from 'chart.js';
import { DatePipe } from '@angular/common';
import { Colors } from 'src/app/shared/Constants/Colors';

const CHART_ID = 'trend_line_chart';
@Component({
	selector: 'app-dashboard-trending-chart',
	templateUrl: './dashboard-trending-chart.component.html',
	styleUrls: ['./dashboard-trending-chart.component.scss'],
})
export class DashboardTrendingChartComponent implements OnInit, AfterViewInit {
	@Input() uniqueId = '';
	isShowingLineChart = true;
	dataFound = true;
	submissionStats = [];
	days = 7;
	params;
	trendChart;

	constructor(
		private submissionSvc: SubmissionsService,
		private activatedRoute: ActivatedRoute,
		private datePipe: DatePipe
	) {}

	ngOnInit() {}

	ngAfterViewInit(): void {
		this.activatedRoute.queryParams.subscribe((params) => {
			this.days = params.days ? params.days : this.days;
			this.getSubmissionCount(params);
		});
	}

	getSubmissionCount(params: any = { days: this.days }) {
		this.params = this.getUpdatedParams(params);
		this.submissionSvc.getSubmissionCount(this.params).subscribe((value: any[]) => {
			this.dataFound = value.length > 0;
			if (!this.dataFound) {
				return;
			}
			this.submissionStats = value;
			this.drawChart();
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

	drawChart() {
		this.isShowingLineChart = true;
		const chart: any = document.getElementById(CHART_ID + this.uniqueId);
		const datasets = { failed: [], warning: [], long: [], delayed: [] };
		const dates = [];

		this.submissionStats.forEach((ss) => {
			const failed = ss[1];
			const warning = ss[2];
			const long = ss[3];
			const delayed = ss[4];
			datasets.failed.push(failed);
			datasets.warning.push(warning);
			datasets.long.push(long);
			datasets.delayed.push(delayed);
			dates.push(this.datePipe.transform(new Date(ss[0]), 'MMM d'));
		});

		if (this.trendChart) {
			this.trendChart.destroy();
		}

		this.trendChart = new Chart(chart.getContext('2d'), {
			type: 'line',
			data: {
				labels: dates.reverse(),
				datasets: [
					{
						label: 'Failed',
						data: datasets.failed.reverse(),
						borderColor: Colors.red,
						backgroundColor: Colors.red,
					},
					{
						label: 'Warning',
						data: datasets.warning.reverse(),
						borderColor: Colors.yellow,
						backgroundColor: Colors.yellow,
					},
					{
						label: 'Long Running',
						data: datasets.long.reverse(),
						borderColor: '#2196f3',
						backgroundColor: '#2196f3',
					},
					{
						label: 'Delayed',
						data: datasets.delayed.reverse(),
						borderColor: Colors.gray,
						backgroundColor: Colors.gray,
					},
				],
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				title: {
					display: false,
					text: `Last ${this.days} days`,
				},
				tooltips: {
					mode: 'index',
					intersect: false,
				},
				hover: {
					mode: 'nearest',
					intersect: true,
				},
				elements: {
					point: {
						radius: 0,
					},
					line: {
						borderDash: [20, 10],
						borderWidth: 4,
						fill: false,
					},
				},
				scales: {
					xAxes: [
						{
							gridLines: {
								drawOnChartArea: false,
							},
						},
					],
					yAxes: [
						{
							gridLines: {
								drawOnChartArea: false,
							},
						},
					],
				},
			},
		});
	}
}
