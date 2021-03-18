import { Component, OnInit, Input, AfterViewInit } from '@angular/core';
import * as Chart from 'chart.js';
import { Colors } from 'src/app/shared/Constants/Colors';
import { draw, generate } from 'patternomaly';

@Component({
	selector: 'app-sla-w2w-details',
	templateUrl: './sla-w2w-details.component.html',
	styleUrls: ['./sla-w2w-details.component.scss']
})
export class SlaW2wDetailsComponent implements AfterViewInit {
	colors = [
		Colors.blue,
		Colors.gray,
		Colors.green,
		Colors.red,
		Colors.yellow,
		Colors.blue,
		Colors.gray,
		Colors.green,
		Colors.red,
		Colors.yellow
	];
	@Input() uniqueId = '';
	stackedChart: Chart;
	submission;
	constructor() {}

	ngAfterViewInit(): void {}

	drawChart(data): void {
		const schedTime =
			(new Date(data.schedEndTime).getTime() - new Date(data.schedStartTime).getTime()) / 60000 + data.tolerance;
		this.drawSLALine(schedTime);

		this.submission = data;
		if (this.stackedChart) {
			this.stackedChart.destroy();
		}
		const chart: any = document.getElementById('w2w_scatter_chart' + this.uniqueId);
		const waitingTime = data.waitingTime;

		const stepsDatasets = [];
		if (waitingTime > 0) {
			stepsDatasets.push({
				data: [waitingTime],
				backgroundColor: Colors.lightgray,
				label: 'Wait Time'
			});
		}
		data.steps.forEach((step, i) => {
			step.isLongRunning = step.durationMins > step.processStep.duration && step.processStep.duration > 0;
			let processName: string = step.processStep.name;
			if (processName === 'start' || processName === 'end') {
				processName = processName.toLocaleUpperCase();
			}

			stepsDatasets.push({
				data: [step.durationMins],
				backgroundColor: step.isLongRunning ? draw('plus', this.colors[i]) : this.colors[i],
				label: processName
			});

			if (i + 1 !== data.steps.length) {
				const waitTime =
					(new Date(data.steps[i + 1].startTime).getTime() - new Date(step.endTime).getTime()) / 60000;
				if (waitTime > 0) {
					stepsDatasets.push({
						data: [waitTime],
						backgroundColor: Colors.lightgray,
						label: 'Wait Time'
					});
				}
			}
		});

		this.stackedChart = new Chart(chart.getContext('2d'), {
			type: 'horizontalBar',
			data: {
				datasets: stepsDatasets,
				labels: ['W2W View']
			},
			options: {
				layout: {
					padding: {
						bottom: 100
					}
				},
				title: {
					text: `Submission ${data.id} - ${data.startTime.split(' ')[0]}`,
					display: true
				},
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
				},
				tooltips: {
					callbacks: {
						label: (tooltipItem, data) => {
							let label = data.datasets[tooltipItem.datasetIndex].label || '';

							// If the first step is Wait Time, Start Time = sched start time and end time = Start step start time
							if (tooltipItem.datasetIndex === 0 && label === 'Wait Time') {
								label += `: ${data.datasets[tooltipItem.datasetIndex]
									.data[0]} minutes (${this.submission.schedStartTime.split(
									' '
								)[1]} - ${this.submission.startTime.split(' ')[1]})`;
							} else if (label !== 'Wait Time') {
								const steps: any[] = this.submission.steps;
								const step = steps.find(
									step => step.processStep.name.toLowerCase() === label.toLowerCase()
								);
								label += `: ${step.durationMins} minutes (${step.startTime.split(
									' '
								)[1]} - ${step.endTime.split(' ')[1]}) ${step.isLongRunning
									? ' [Normally ' + step.processStep.duration + ' Minutes]'
									: ''}`;
							} else {
								const steps: any[] = this.submission.steps;
								const prevLabel = data.datasets[tooltipItem.datasetIndex - 1].label;
								const nextLabel = data.datasets[tooltipItem.datasetIndex + 1].label;
								const prevStep = steps.find(
									step => step.processStep.name.toLowerCase() === prevLabel.toLowerCase()
								);
								const nextStep = steps.find(
									step => step.processStep.name.toLowerCase() === nextLabel.toLowerCase()
								);
								label += `: ${data.datasets[tooltipItem.datasetIndex]
									.data[0]} minutes (${prevStep.endTime.split(' ')[1]} - ${nextStep.startTime.split(
									' '
								)[1]})`;
							}
							return label;
						}
					}
				}
			}
		});
	}

	drawSLALine(markerValue) {
		Chart.helpers.extend(Chart.controllers.horizontalBar);
		const verticalLinePlugin = {
			beforeDraw: chart => {
				if (chart.config.type !== 'horizontalBar') {
					return;
				}
				const scale = chart.scales['y-axis-0'];
				const xScale = chart.scales['x-axis-0'];
				const context = chart.chart.ctx;
				context.clearRect(0, 0, chart.width, chart.height);

				// render vertical line
				context.setLineDash([8, 8]);
				context.beginPath();
				context.strokeStyle = Colors.red;
				context.moveTo(xScale.getPixelForValue(markerValue), scale.top);
				context.lineTo(xScale.getPixelForValue(markerValue), scale.bottom);
				context.stroke();

				// write label
				context.fillStyle = Colors.red;
				context.textAlign = 'center';
				context.fillText('SLA Time', xScale.getPixelForValue(markerValue), scale.top - 5);
			}
		};

		Chart.plugins.register(verticalLinePlugin);
	}
}
