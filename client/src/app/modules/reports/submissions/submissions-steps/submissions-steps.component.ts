import { Component, OnInit, Input, Output, OnDestroy, ViewEncapsulation, EventEmitter } from '@angular/core';
import { SubmissionsService } from '../submissions.service';
import { DateCommon } from 'src/app/shared/DateCommon';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { GoogleCharts } from 'google-charts';
import { DatePipe } from '@angular/common';
import * as moment from 'moment-timezone';

@Component({
	selector: 'app-submissions-steps',
	templateUrl: './submissions-steps.component.html',
	styleUrls: ['./submissions-steps.component.scss'],
	encapsulation: ViewEncapsulation.None
})
export class SubmissionsStepsComponent implements OnInit, OnDestroy {
	@Input() scheduledSubmission;
	@Input() autoRefreshOn;
	@Output() stepsRefresh = new EventEmitter();

	selectedStep;
	steps = [];
	activeIndex = 0;
	autorefreshHandler;
	lastUpdatedTime;
	data;
	displaySubmissionDialog = false;
	submissionNotes;

	displayNotesDialog = false;
	notesData: any;

	startTime: Date;
	endTime: Date;

	@Input() processId: 0;

	constructor(private submissionsSvc: SubmissionsService, private datePipe: DatePipe) {}

	ngOnInit() {
		this.getSubmissionSteps(true);

		this.autorefreshHandler = setInterval(() => {
			if (this.autoRefreshOn) {
				this.getSubmissionSteps(false);
			}
		}, 1000 * 30);
	}

	ngOnDestroy() {
		clearInterval(this.autorefreshHandler);
	}

	getSubmissionSteps(updateStep) {
		moment.tz.setDefault('America/New_York');

		this.steps = [];
		this.submissionsSvc.getSubmissionSteps(this.processId).subscribe(values => {
			const allDates = [];
			values.map(ps => {
				let startMoment = moment(ps.startTime);
				ps.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m:s a');
				if (ps.endTime) {
					let endMoment = moment(ps.endTime);
					ps.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m:s a');
				}

				this.steps.push({
					label: `${ps.processStep.name}`,
					status: ps.status.name,
					notes: ps.notes,
					styleClass: ps.status.name.replace(/ /g, ''),
					startTime: ps.startTime,
					endTime: ps.endTime,
					elapsedTime: DateCommon.dateDifference(ps.startTime, ps.endTime ? ps.endTime : new Date(), false),
					duration: ps.processStep.duration
				});
				allDates.push(new Date(ps.startTime).getTime());
				allDates.push(new Date(ps.endTime).getTime());
			});
			this.stepsRefresh.emit({ id: values[0].submissionId.toString() });
			if (updateStep) {
				this.activeIndex = values.length - 1;
				this.selectedStep = this.steps[values.length - 1];
			}
			// Get latest step start/end date.
			this.lastUpdatedTime = new Date(Math.max(...allDates));
			GoogleCharts.load(
				_ => {
					this.drawChart();
				},
				{ packages: ['timeline'] }
			);
		});
	}

	onSelectStep(event) {
		this.selectedStep = this.steps[event];
	}

	openNotesDialog(step) {
		this.notesData = step.notes;
		this.displayNotesDialog = true;
	}
	minutesToMilliseconds(minutes) {
		return minutes * 60 * 1000;
	}

	// workaround for issue with moment timezone format: using moment timezone format converts again the date to America/New_York
	nativeDateFormat(date: Date): string {
		let hour = date.getHours();
		let minute = date.getMinutes();
		let second = date.getSeconds();
		let am_pm = hour > 11 ? 'PM' : 'AM';
		if (hour > 12) {
			hour = hour - 12;
		}
		return `${hour}:${minute > 9 ? minute : '0' + minute}:${second > 9 ? second : '0' + second} ${am_pm}`;
	}

	generateTooltip(stepName: string, stepStatus: string, start: Date, end: Date, notes: string): string {
		return `<div style="width: 300px">
  <div style="padding: 10px 10px 5px 10px; border-bottom: solid 1px black">
    <p><b>${stepStatus}</b></p>
  </div>
  <div style="padding: 5px 10px 10px 10px;">
    <p><b>${stepName}:</b> ${this.nativeDateFormat(start)} - ${this.nativeDateFormat(end)}</p>
    <p><b>Duration:</b> ${DateCommon.dateDifference(start, end, false)}</p>
    <p><b>Notes:</b> ${notes}</p>
  </div>
</div>`;
	}

	drawChart() {
		const container = document.getElementById(`gant_chart_${this.processId}`);
		const chart = new GoogleCharts.api.visualization.Timeline(container);
		const dataTable = new GoogleCharts.api.visualization.DataTable();
		let chartHeight = 350;

		dataTable.addColumn({ type: 'string', id: 'Step' });
		dataTable.addColumn({ type: 'string', id: 'dummy bar label' });
		//    dataTable.addColumn({ type: 'string',  });
		dataTable.addColumn({ type: 'string', id: 'style', role: 'style' });
		dataTable.addColumn({ type: 'string', role: 'tooltip', p: { html: true } });
		dataTable.addColumn({ type: 'date', id: 'Start' });
		dataTable.addColumn({ type: 'date', id: 'End' });

		this.startTime = new Date(this.steps[0].startTime);

		for (let i = 0; i < this.steps.length; i++) {
			const startTime = new Date(this.steps[i].startTime);
			let iconNotes = '';
			let iconColor = '';

			if (this.steps[i].notes != null) {
				iconColor = '; stroke-color: #085402';
				iconNotes = ' *';
			}

			if (this.steps[i].endTime != null) {
				this.endTime = new Date(this.steps[i].endTime);
			} else {
				this.endTime = new Date();
				this.steps[i].duration = this.steps[i].duration > 0 ? this.steps[i].duration : 1;
				const expectedEndTime = new Date(startTime.getTime());
				expectedEndTime.setMinutes(expectedEndTime.getMinutes() + this.steps[i].duration);
				dataTable.addRows([
					[
						this.steps[i].label,
						'Expected Duration',
						'color: lightblue',
						this.generateTooltip(this.steps[i].label, 'Expected Duration', startTime, expectedEndTime, ''),
						startTime,
						expectedEndTime
					]
				]);
			}

			const diff = this.endTime.getTime() - startTime.getTime();

			if (diff === 0) {
				this.endTime.setTime(this.endTime.getTime() + 1000);
			}

			dataTable.addRows([
				[
					this.steps[i].label,
					this.steps[i].status + iconNotes,
					'color: ' + SubmissionCommon.submissionStatusColor(this.steps[i].status) + iconColor,
					this.generateTooltip(
						this.steps[i].label,
						this.steps[i].status,
						startTime,
						this.endTime,
						this.steps[i].notes || ''
					),
					startTime,
					this.endTime
				]
			]);
		}

		if (this.scheduledSubmission) {
			let startMoment = moment(this.scheduledSubmission.startTime);
			let endMoment = moment(this.scheduledSubmission.endTime);
			this.scheduledSubmission.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m:s a');
			this.scheduledSubmission.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m:s a');
			dataTable.addRows([
				[
					'start',
					'Expected Start Time',
					'color: ' + 'lightblue',
					this.generateTooltip(
						'start',
						'Expected Start Time',
						new Date(this.scheduledSubmission.startTime),
						new Date(this.scheduledSubmission.startTime),
						''
					),
					new Date(this.scheduledSubmission.startTime),
					new Date(this.scheduledSubmission.startTime)
				]
			]);

			dataTable.addRows([
				[
					'end',
					'Expected End Time',
					'color: lightblue',
					this.generateTooltip(
						'end',
						'Expected End Time',
						new Date(this.scheduledSubmission.endTime),
						new Date(this.scheduledSubmission.endTime),
						''
					),
					new Date(this.scheduledSubmission.endTime),
					new Date(this.scheduledSubmission.endTime)
				]
			]);
		}

		if (this.steps.length === 1) {
			chartHeight = 130;
		} else if (this.steps.length <= 5) {
			chartHeight = this.steps.length * 65;
		}

		const options = {
			height: chartHeight,
			timeline: {
				rowLabelStyle: { fontSize: 14 }
			},
			tooltip: { isHtml: true }
		};

		GoogleCharts.api.visualization.events.addListener(chart, 'ready', e => {
			let haxis: NodeListOf<Element>;
			for (let index = 1; index <= 3; index++) {
				haxis = container.querySelector('svg').querySelectorAll(`g > text[font-size='13']`);
				if (haxis.length > 0) {
					break;
				}
			}
			const hasBadFormat: boolean = Array.from(haxis).filter(x => x.textContent === '30').length > 2;

			if (!hasBadFormat) {
				return;
			}

			const nodes = Array.from(haxis);

			// Find seconds
			let seconds = 0;
			for (let index = 1; index < nodes.length; index++) {
				let current = Number.parseInt(nodes[index].textContent);
				let prev = Number.parseInt(nodes[index - 1].textContent);
				current = current === 0 ? 60 : current;
				prev = prev === 0 ? 60 : prev;
				seconds = Math.abs(current - prev);
				break;
			}

			nodes.forEach((child, index) => {
				if ((index + 1) % 2 > 0) {
					const newTime = new Date(this.startTime.getTime());
					newTime.setSeconds(newTime.getSeconds() + index * seconds);
					child.textContent = this.datePipe.transform(newTime, 'H:mm');
				} else {
					child.textContent = '';
				}
			});
			const firstNode = nodes[0];
			const lastNode = nodes[nodes.length - 1];
			firstNode.textContent = this.datePipe.transform(this.startTime, 'H:mm:s');
			firstNode.setAttribute('font-weight', 'bold');
			lastNode.textContent = this.datePipe.transform(this.endTime, 'H:mm:s');
			lastNode.setAttribute('font-weight', 'bold');
		});

		GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
			this.changeMousePointerOut();
			this.showDialog(chart.getSelection()[0].row);
		});

		GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
			this.changeMousePointerOn();
		});

		GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
			this.changeMousePointerOut();
		});

		chart.draw(dataTable, options);
	}

	showDialog(row) {
		if (this.steps[row].notes != null) {
			this.notesData = this.steps[row].notes;
			this.displayNotesDialog = true;
		}
	}

	changeMousePointerOn() {
		document.body.style.cursor = 'pointer';
	}

	changeMousePointerOut() {
		document.body.style.cursor = 'initial';
	}
}
