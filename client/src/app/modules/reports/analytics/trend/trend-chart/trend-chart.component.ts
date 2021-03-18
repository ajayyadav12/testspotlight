import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import * as c3 from 'c3';

@Component({
	selector: 'app-trend-chart',
	templateUrl: './trend-chart.component.html',
	styleUrls: ['./trend-chart.component.scss'],
})
export class TrendChartComponent implements OnInit {
	@Input() dataToLoad;
	@Input() deviationMap;
	@Input() outlinerOption;
	@Input() countValue;
	@Output() outlinerMapping = new EventEmitter();

	get stats() {
		return this.deviationMap.info.stats;
	}

	periodDays = [];
	groups = [
		{ label: 'By Status', value: true },
		{ label: 'By AdHoc', value: false },
	];
	loadDefault = true;
	chartData;
	outlinerValue: String;
	private chart: any;
	submissionId: number;

	constructor() {}

	ngOnInit() {
		this.setPeriodDays();
		this.prepareChartData();
		this.generateChart();
		// alert(this.outlinerValue);
	}

	/**
	 * Create formatted date labels (YYYY-MM-DD) for each day in period
	 */
	setPeriodDays() {
		const start = new Date(this.deviationMap.info.report.rangeStart);
		const end = new Date(this.deviationMap.info.report.rangeEnd);
		const length = this.deviationMap.info.report.rangeLength;

		this.periodDays.push(start.toISOString().split('T')[0]);

		if (length > 1) {
			let tempDate = new Date(new Date(start).setDate(start.getDate() + 1));

			while (
				tempDate.getFullYear() < end.getFullYear() ||
				(tempDate.getFullYear() === end.getFullYear() &&
					(tempDate.getMonth() < end.getMonth() ||
						(tempDate.getMonth() === end.getMonth() && tempDate.getDate() < end.getDate())))
			) {
				this.periodDays.push(tempDate.toISOString().split('T')[0]);
				tempDate = new Date(new Date(tempDate).setDate(tempDate.getDate() + 1));
			}
		}

		if (length > 2) {
			this.periodDays.push(end.toISOString().split('T')[0]);
		}
	}

	/**
	 * Call setGroups() and setYMarkers() to map incoming data
	 */
	prepareChartData() {
		const data = this.setGroups();

		this.chartData = {
			period: data[0],
			statusSubmissions: data[1],
			statusPoints: data[2],
			scheduleSubmissions: data[3],
			schedulePoints: data[4],
			xMarkers: data[5],
			yMarkers: this.setYMarkers(),
		};
	}

	/**
	 * Use period days to create groups of data points in chart
	 * For each date in the period, add data point for each submission that ends on that date
	 * If a date in the period has no submissions that end on that date, add a null data point (to maintain index across groups)
	 * Create groups (with matching index) for status, schedule/adHoc
	 * Create optional xMarkers to show dates that have adHoc submissions
	 */
	setGroups() {
		const period: any[] = ['Period'];

		const statusSubmissions: any[] = [['Success'], ['Warning'], ['Failed']];
		const statusPoints: any[] = [['Success'], ['Warning'], ['Failed']];

		const scheduleSubmissions: any[] = [['Scheduled'], ['AdHoc']];
		const schedulePoints: any[] = [['Scheduled'], ['AdHoc']];

		const xMarkers: any[] = [];

		this.periodDays.forEach((day) => {
			let found = false;
			const adHocFound = false;

			this.dataToLoad.forEach((s) => {
				const sub = s.submission;

				if (sub.endTime.split('T')[0] === day) {
					found = true;

					period.push(day);

					statusSubmissions[0].push(sub.status.name === 'success' ? s : null);
					statusPoints[0].push(sub.status.name === 'success' ? s.duration / 60 : null);

					statusSubmissions[1].push(sub.status.name === 'warning' ? s : null);
					statusPoints[1].push(sub.status.name === 'warning' ? s.duration / 60 : null);

					statusSubmissions[2].push(sub.status.name === 'failed' ? s : null);
					statusPoints[2].push(sub.status.name === 'failed' ? s.duration / 60 : null);

					scheduleSubmissions[0].push(sub.adHoc ? null : s);
					schedulePoints[0].push(sub.adHoc ? null : s.duration / 60);

					scheduleSubmissions[1].push(sub.adHoc ? s : null);
					schedulePoints[1].push(sub.adHoc ? s.duration / 60 : null);

					if (sub.adHoc && !adHocFound) {
						xMarkers.push({ value: day, label: 'AdHoc' });
					}
				}
			});

			if (!found) {
				period.push(day);

				statusSubmissions[0].push(null);
				statusPoints[0].push(null);

				statusSubmissions[1].push(null);
				statusPoints[1].push(null);

				statusSubmissions[2].push(null);
				statusPoints[2].push(null);

				scheduleSubmissions[0].push(null);
				schedulePoints[0].push(null);

				scheduleSubmissions[1].push(null);
				schedulePoints[1].push(null);
			}
		});

		return [period, statusSubmissions, statusPoints, scheduleSubmissions, schedulePoints, xMarkers];
	}

	/**
	 * Prepare duration (Y) markers for graph based data stats in deviationMap[]
	 */
	setYMarkers() {
		const yMarkers = [];

		if (this.stats.count > 2) {
			yMarkers.push({ value: this.stats.median / 60, text: 'Median' });
		}

		if (this.deviationMap.info.labels.avgHist === this.deviationMap.info.labels.avgPeriod) {
			yMarkers.push({
				value: this.stats.avg.period / 60,
				text: 'Overall Average',
			});
		} else {
			yMarkers.push({
				value: this.stats.avg.period / 60,
				text: 'Period Average',
			});
			yMarkers.push({
				value: this.stats.avg.hist / 60,
				text: 'Historical Average',
			});
		}

		if (this.stats.schedule.value) {
			yMarkers.push({
				value: this.stats.schedule.value / 60,
				text: 'Schedule',
			});
		}

		return yMarkers;
	}

	/**
	 * Generate c3js chart using prepared chart data
	 */
	generateChart() {
		const period = this.chartData.period;

		const statusSubmissions = this.chartData.statusSubmissions;
		const statusPoints = this.chartData.statusPoints;

		const scheduleSubmissions = this.chartData.scheduleSubmissions;

		const yMarkers = this.chartData.yMarkers;

		this.chart = c3.generate({
			bindto: '#chartTrend',
			size: {
				height: 400,
			},
			data: {
				x: 'Period',
				columns: [period, statusPoints[0], statusPoints[1], statusPoints[2]],
				type: 'scatter',
				colors: {
					Success: '#3daf66',
					Warning: '#e8a325',
					Failed: '#db4c41',
					Scheduled: '#3c97e0',
					AdHoc: '#7c7c7c',
				},
			},
			point: {
				r: 5,
			},
			zoom: {
				enabled: true,
			},
			legend: {
				position: 'right',
			},
			tooltip: {
				format: {
					title: function (x) {
						return 'Click to show submission details';
					},
					value: function (value, ratio, id, index) {
						const defaultView = id === 'Success' || id === 'Warning' || id === 'Failed' ? true : false;

						const data =
							id === 'Success'
								? statusSubmissions[0][index + 1]
								: id === 'Warning'
								? statusSubmissions[1][index + 1]
								: id === 'Failed'
								? statusSubmissions[2][index + 1]
								: id === 'Scheduled'
								? scheduleSubmissions[0][index + 1]
								: scheduleSubmissions[1][index + 1];

						const info = defaultView
							? data.submission.adHoc
								? 'adHoc'
								: 'scheduled'
							: data.submission.status.name;

						return '#' + data.submission.id + ' (' + data.elapsedTime + ' - ' + info + ')';
					},
				},
			},
			axis: {
				x: {
					label: 'End Date (M/D)',
					type: 'timeseries',
					tick: {
						fit: false,
					},
				},
				y: {
					label: 'Duration (m)',
				},
			},
			grid: {
				y: {
					lines: yMarkers,
				},
			},
		});
	}

	/**
	 * Toggle between status view and schedule view
	 * Swap data maps
	 */
	toggleChartData() {
		if (this.loadDefault) {
			this.chart.load({
				unload: ['Scheduled', 'AdHoc'],
				columns: [
					this.chartData.statusPoints[0],
					this.chartData.statusPoints[1],
					this.chartData.statusPoints[2],
				],
			});
		} else {
			this.chart.load({
				unload: ['Success', 'Warning', 'Failed'],
				columns: [this.chartData.schedulePoints[0], this.chartData.schedulePoints[1]],
			});
		}
	}

	generateOutliners(value) {
		this.outlinerMapping.emit(value);
	}
}
