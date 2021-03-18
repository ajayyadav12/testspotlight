import { Component, OnInit, AfterViewInit, Input } from '@angular/core';
import { Colors } from 'src/app/shared/Constants/Colors';
import * as Chart from 'chart.js';

@Component({
	selector: 'app-sla-trend',
	templateUrl: './sla-trend.component.html',
	styleUrls: ['./sla-trend.component.scss']
})
export class SlaTrendComponent implements AfterViewInit {
	stackedChart;
	@Input() uniqueId = '';
	constructor() {}

	ngAfterViewInit(): void {}

	drawChart(data: any[]): void {
		const schedTime =
			(new Date(data[0].schedEndTime).getTime() - new Date(data[0].schedStartTime).getTime()) / 60000 +
			data[0].tolerance;
		let sum = 0;
		data.forEach(d => (sum += d.durationMins));
		const avg = sum / data.length;
		this.drawSLALine(schedTime, avg);

		if (this.stackedChart) {
			this.stackedChart.destroy();
		}
		const chart: any = document.getElementById('trend_scatter_chart' + this.uniqueId);
		const waitingTime = [];
		const durationMins = [];
		const outOfSLA = [];
		const dates = [];
		const colors = { lightgray: [], green: [], yellow: [] };

		data.reverse().forEach(row => {
			waitingTime.push(row.waitingTime);
			durationMins.push(row.durationMins);
			outOfSLA.push(row.outOfSLA);
			dates.push(row.schedStartTime);
			colors.lightgray.push(Colors.lightgray);
			colors.green.push(Colors.green);
			colors.yellow.push(Colors.yellow);
		});

		this.stackedChart = new Chart(chart.getContext('2d'), {
			type: 'bar',
			data: {
				datasets: [
					{
						data: waitingTime,
						backgroundColor: colors.lightgray,
						label: 'Wait Time'
					},
					{
						data: durationMins,
						backgroundColor: colors.green,
						label: 'Time Spent'
					},
					{
						data: outOfSLA,
						backgroundColor: colors.yellow,
						label: 'Out of SLA'
					}
				],
				labels: dates
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					xAxes: [
						{
							stacked: true
						}
					],
					yAxes: [
						{
							stacked: true
						}
					]
				}
			}
		});
	}

	drawSLALine(markerValue, avgValue) {
		Chart.helpers.extend(Chart.controllers.bar);
		const verticalLinePlugin = {
			beforeDraw: chart => {
				if (chart.config.type !== 'bar') {
					return;
				}
				const scale = chart.scales['y-axis-0'];
				const xScale = chart.scales['x-axis-0'];
				const context = chart.chart.ctx;
				context.clearRect(0, 0, chart.width, chart.height);

				// render horizontal line
				context.setLineDash([8, 8]);
				context.beginPath();
				context.strokeStyle = Colors.red;
				context.moveTo(70, scale.getPixelForValue(markerValue));
				context.lineTo(chart.width, scale.getPixelForValue(markerValue));
				context.stroke();

				// write label
				context.fillStyle = Colors.red;
				context.textAlign = 'center';
				context.fillText('SLA Time', 25, scale.getPixelForValue(markerValue) + 10);

				// // render horizontal line
				// context.setLineDash([8, 8]);
				// context.beginPath();
				// context.strokeStyle = Colors.gray;
				// context.moveTo(70, scale.getPixelForValue(avgValue));
				// context.lineTo(chart.width, scale.getPixelForValue(avgValue));
				// context.stroke();

				// // write label
				// context.fillStyle = Colors.gray;
				// context.textAlign = 'center';
				// context.fillText('Avg. Total Time', 25, scale.getPixelForValue(avgValue) + 10);
			}
		};

		Chart.plugins.register(verticalLinePlugin);
	}
}
