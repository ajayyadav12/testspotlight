import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import * as c3 from 'c3';

@Component({
	selector: 'app-variance-chart',
	templateUrl: './variance-chart.component.html',
	styleUrls: ['./variance-chart.component.scss'],
})
export class VarianceChartComponent implements OnInit {
	private chart: any;

	@Input() deviationMap;
	@Output() loading = new EventEmitter();

	get stats() {
		return this.deviationMap.info.stats;
	}

	get ranges() {
		return [
			this.deviationMap.minus,
			this.deviationMap.minus2,
			this.deviationMap.minus1,
			this.deviationMap.plus1,
			this.deviationMap.plus2,
			this.deviationMap.plus,
		];
	}

	constructor() {}

	ngOnInit() {
		this.generateChart();
	}

	/**
	 * Prepare markers for graph based data in deviationMap[]
	 */
	setMarkers() {
		const markers = [];

		if (this.stats.count > 2) {
			markers.push({ value: this.stats.median, text: 'Median' });
		}

		if (this.deviationMap.info.labels.avgHist === this.deviationMap.info.labels.avgPeriod) {
			markers.push({ value: this.stats.avg.period, text: 'Overall Average' });
		} else {
			markers.push({ value: this.stats.avg.period, text: 'Period Average' });
			markers.push({ value: this.stats.avg.hist, text: 'Historical Average' });
		}

		if (this.stats.schedule.value) {
			markers.push({ value: this.stats.schedule.value, text: 'Schedule' });
		}

		return markers;
	}

	/**
	 * Generate c3js chart using data in deviationMap[]
	 * Call setMarkers() to retrieve markers
	 */
	generateChart() {
		const stats = this.stats;
		const deviationMap = this.deviationMap;
		const ranges = this.ranges;

		const markers = this.setMarkers();

		this.chart = c3.generate({
			bindto: '#chartVariance',
			size: {
				height: 300,
			},
			data: {
				x: 'x',
				columns: [
					[
						// to display deviation ranges *between* ticks, offset each x value
						'x',
						this.deviationMap.minus.value + this.stats.stddev / 2,
						this.deviationMap.minus2.value + this.stats.stddev / 2,
						this.deviationMap.minus1.value + this.stats.stddev / 2,
						this.deviationMap.plus1.value - this.stats.stddev / 2,
						this.deviationMap.plus2.value - this.stats.stddev / 2,
						this.deviationMap.plus.value - this.stats.stddev / 2,
					],
					[
						'count',
						this.deviationMap.minus.data.length,
						this.deviationMap.minus2.data.length,
						this.deviationMap.minus1.data.length,
						this.deviationMap.plus1.data.length,
						this.deviationMap.plus2.data.length,
						this.deviationMap.plus.data.length,
					],
				],
				type: 'bar',
				labels: true,
				color: function (inColor, data) {
					const colors = ['#db4c41', '#e8a325', '#3daf66', '#3daf66', '#e8a325', '#db4c41'];
					if (data.index !== undefined) {
						return colors[data.index];
					}
					return inColor;
				},
			},
			bar: {
				width: {
					ratio: 0.7,
				},
			},
			legend: {
				show: false,
			},
			tooltip: {
				format: {
					title: function (x) {
						if (x < deviationMap.minus2.value) {
							return deviationMap.minus.labels.title;
						} else if (x < deviationMap.minus1.value) {
							return deviationMap.minus2.labels.title;
						} else if (x < stats.avg.period) {
							return deviationMap.minus1.labels.title;
						} else if (x > deviationMap.plus2.value) {
							return deviationMap.plus.labels.title;
						} else if (x > deviationMap.plus1.value) {
							return deviationMap.plus2.labels.title;
						} else {
							return deviationMap.plus1.labels.title;
						}
					},
				},
			},
			axis: {
				x: {
					label: 'Variance Range',
					tick: {
						// since x values of deviation ranges are offset, create ticks manually
						values: [
							this.deviationMap.minus2.value,
							this.deviationMap.minus1.value,
							this.stats.avg.period,
							this.deviationMap.plus1.value,
							this.deviationMap.plus2.value,
						],
						format: function (x) {
							if (x === deviationMap.minus2.value) {
								return deviationMap.minus2.labels.value;
							} else if (x === deviationMap.minus1.value) {
								return deviationMap.minus1.labels.value;
							} else if (x === stats.avg.period) {
								return deviationMap.info.labels.avgPeriod;
							} else if (x === deviationMap.plus1.value) {
								return deviationMap.plus1.labels.value;
							} else if (x === deviationMap.plus2.value) {
								return deviationMap.plus2.labels.value;
							} else {
								return '';
							}
						},
					},
				},
				y: {
					label: 'Submission Count (#)',
				},
			},
			grid: {
				x: {
					lines: markers,
				},
			},
		});
	}
}
