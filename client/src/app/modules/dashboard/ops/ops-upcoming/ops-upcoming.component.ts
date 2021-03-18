import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ScheduleSubmissionsService } from '../../../admin/schedule/schedule-submissions.service';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { SessionService } from 'src/app/core/session/session.service';
import { MessageService, SelectItem } from 'primeng/api';
import { ActivatedRoute } from '@angular/router';
import { GeUpdateScheduledSubmissionComponent } from 'src/app/shared/Components/ge-update-scheduled-submission/ge-update-scheduled-submission.component';
import * as moment from 'moment-timezone';
import * as Chart from 'chart.js';
import { DatePipe } from '@angular/common';
import { Colors } from 'src/app/shared/Constants/Colors';

@Component({
  selector: 'app-ops-upcoming',
  templateUrl: './ops-upcoming.component.html',
  styleUrls: ['./ops-upcoming.component.scss']
})
export class OpsUpcomingComponent implements OnInit {
  @Input() isLightVersion: boolean;
	@Input() editNotes: string;
	@Input() uniqueId = '';
	@ViewChild('updateScheduledSubmission', { static: true })
	updateScheduledSubmissionComponent: GeUpdateScheduledSubmissionComponent;

	scheduledSubmissions;
	dataFound = true;
	displayAcknowledgmentDialog = false;
	acknowledgementData: GENotes;
	displayDisabledDialog = false;
	disableNoteData: GENotes;
	editNoteData: GENotes;
	loading = false;
	displayType = 2;
	display = false;
	displayTableDialog = false;
	submissionList2 = true;
	params;
	data4count = [];
	columns = [
		{ header: 'ID', field: 'id', width: '4%' },
		{ header: 'Process', field: 'processName', width: '12%' },
		{ header: 'Planned Start', field: 'start', width: '9%' },
		{ header: 'Planned End', field: 'end', width: '9%' }
	];
	types: SelectItem[] = [
		{ label: 'Table', value: 0, icon: 'pi pi-table' },
		{ label: 'Calendar', value: 1, icon: 'pi pi-calendar' },
		{ label: 'Chart', value: 2 }
	];

	get menuItems() {
		if (this.sessionSvc.role !== 'user') {
			return [ { label: 'Acknowledge Delay' }, { label: 'Disable submission' }, { label: 'Edit submission' } ];
		} else {
			return null;
		}
	}
	constructor(
		private msgSvc: MessageService,
		private sessionSvc: SessionService,
		private scheduleSubmissionsSvc: ScheduleSubmissionsService,
		private activatedRoute: ActivatedRoute,
		private datePipe: DatePipe
	) {}

	ngAfterViewInit(): void {
		this.activatedRoute.queryParams.subscribe((params) => {
			if (!(params.systemId )) {
				this.getScheduledSubmissions();
			} else {
				this.getFilteredScheduleSubmission(params);
			}
		});
		const tab = localStorage.getItem('dashboard-submission-tab');
		if (tab) {
			this.updateTab({ value: tab });
		}
	}

	drawScatterChart() {
		const chart: any = document.getElementById('upcoming_scatter_chart' + this.uniqueId);
		const dayLabels = [];
		for (let i = 0; i <= 14; i++) {
			let day = new Date();
			day.setDate(day.getDate() + i);

			dayLabels.push(this.datePipe.transform(day, 'MMMM d'));
		}

		let daymax = new Date();
		daymax.setDate(daymax.getDate() + 11);

		const top = [];
		const bottom = [];
		const threeruns = [];
		const fourruns = [];
		const onerun = [];
		const tworuns = [];
		const processNames = [];
		const processIds = [];

		
		top.push({x: daymax , y: ' '});
		processNames.push(' ');
		this.scheduledSubmissions.forEach((sched) => {		
			const date = new Date(sched.startTime);
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);

			this.data4count.push({mdate: date,process: sched.procesId});
		});

		this.scheduledSubmissions.forEach((sched) => {
			const date = new Date(sched.startTime);
			let runs;
			date.setHours(0);
			date.setMinutes(0);
			date.setSeconds(0);
			const hours = new Date(sched.startTime).getHours();



				//submissionData.push({x: date, y: sched.procesId});


			runs = this.countRuns(date, sched.procesId);		
			
			if (date < daymax) {
				if (processNames.find(x => sched.processName === x) == undefined) {
					processNames.push(sched.processName);
				}

				//if upcoming runs is a particular number
				if (runs == 1) {
					onerun.push({x: date , y: sched.processName});
				} else if (runs == 2) {
					tworuns.push({x: date, y: sched.procesName});
				} else if (sched.procesId == 3) {
					threeruns.push({x: date, y: sched.procesName});
				} else {
					fourruns.push({x: date, y: sched.procesName});
				}
			} 
		});
		bottom.push({x: daymax , y: ''});
		processNames.push('');

		const dateMin = this.datePipe.transform(new Date(), 'MM-dd-yyyy');

		let dayMax = new Date();
		dayMax.setDate(dayMax.getDate() + 10);
		const dateMax = this.datePipe.transform(dayMax, 'MM-dd-yyyy');


		
		new Chart(chart.getContext('2d'), {
			type: 'bubble',			
			data: {
				labels: processNames,
				datasets: [
					{
						label: '1 run',
            			backgroundColor: Colors.green,
						radius: 4,
						borderColor: Colors.green,
						hoverRadius: 5,
						data: top,
						showLine: false
					},
					{
						label: '1 run',
            			backgroundColor: Colors.green,
						radius: 4,
						borderColor: Colors.green,
						hoverRadius: 5,
						data: onerun,
						showLine: false
					},
					{
						label: '2 runs',
            			backgroundColor: Colors.green,
						radius: 6,
						borderColor: Colors.green,
						hoverRadius: 7,
						data: tworuns,
						showLine: false
					},
					{
						label: '3 runs',
            			backgroundColor: Colors.green,
						radius: 8,
						borderColor: Colors.green,
						hoverRadius: 9,
						data: threeruns,
						showLine: false
					},
					{
						label: '>4',
            			backgroundColor: Colors.green,
						radius: 9,
						borderColor: Colors.green,
						hoverRadius: 10,
						data: fourruns,
						showLine: false
					},		
					{
						label: '1 run',
            			backgroundColor: Colors.green,
						radius: 3,
						borderColor: Colors.green,
						hoverRadius: 5,
						data: bottom,
						showLine: false
					},													
				]
			},
			plugins: [{
				beforeInit: function(chart, options) {
				  chart.options.legend.display = false; 
				  
				}
			  }],
			options: {
				legend: {	
							labels: {boxWidth:50 } },
        		responsive: true,
        		//showLine: false,
				maintainAspectRatio: false,
				title: {
					display: false,
					text: 'Last'
				},
				tooltips: {
					callbacks: {
						title: (node) => {
							return node[0].value;
						}
					}
				},
				scales: {
					
					gridLines: {
						display: false
					},
					xAxes: [
						{
							type: 'time',
							time: { unit: 'day', unitStepSize: 1, min: dateMin, max: dateMax}, 
							position: 'bottom'
						}
					],
					yAxes: [
						{					
							type: 'category',								
							ticks: {
								source: 'labels',
							
								callback: (v) => {
									const sched2 = this.scheduledSubmissions.find(
										(x) => x.procesId === v
									);
									if (sched2 != undefined){
										return sched2.processName;
									} else { return v}

								}
							}
						}
					]
				}
			}
		});
	}

	getFilteredScheduleSubmission(params: any = { scheduledSubmissions: this.scheduledSubmissions }) {
		this.params = {
			sender: params.systemId ? params.systemId : '-1,',
			receiver: params.systemId ? params.systemId : '-1,'
		};
		this.loading = true;
		this.scheduleSubmissionsSvc.getScheduleSubmissions(this.params).subscribe((value: any[]) => {
			this.dataFound = value.length > 0;
			if (!this.dataFound) {
				this.submissionList2 = false;
				return;
			}
			this.scheduledSubmissions = value;
			this.scheduledSubmissions.map((s) => {
				this.scheduleSubmissionMapping(s);
			});
			this.submissionList2 = true;
			this.drawScatterChart();
			this.loading = false;
		});
	}

	ngOnInit() {}

	showDialog(event) {
		this.display = false;
	}

	updateTab(event) {
		this.displayType = Number.parseFloat(event.value);
		localStorage.setItem('dashboard-submission-tab', event.value);
	}

	getScheduledSubmissions() {
		const date1 = new Date(new Date().setDate(new Date().getDate())).toISOString().split('T')[0];
		const date2 = new Date(new Date().setDate(new Date().getDate() + 90)).toISOString().split('T')[0];
		this.loading = true;
		this.scheduleSubmissionsSvc.getExpectedSubmissions(date1, date2).subscribe((value: any[]) => {
			this.scheduledSubmissions = value;
			this.scheduledSubmissions.map((s) => {
				this.scheduleSubmissionMapping(s);
			});
			this.submissionList2 = true;
			this.drawScatterChart();
			this.loading = false;
		});
	}

	scheduleSubmissionMapping(s) {
		moment.tz.setDefault('America/New_York');
		let startMoment = moment(s.startTime);
		let endMoment = moment(s.endTime);

		s.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
		s.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');

		const now = new Date();
		s.processName = s.process === null ? s.procesName : s.process.name;
		s.start = new Date(s.startTime).toLocaleString();
		const delayedTime = new Date(s.startTime);
		delayedTime.setMinutes(delayedTime.getMinutes() + 15 + s.tolerance);
		s.end = new Date(s.endTime).toLocaleString();
		s.cursor = 'pointer';
		if (!s.disabled) {
			const isDelayed = !s.acknowledgementFlag && now.getTime() > delayedTime.getTime();
			s.backgroundColor = isDelayed ? '#f080805c' : null;
			s.tooltipValue = isDelayed ? 'Delayed' : null;
		}
		s.id = s.acknowledgementFlag ? s.id : s.id;
		s.tooltipValue = s.disabled ? 'Disabled' : s.tooltipValue;
		s.color = s.disabled ? 'lightsteelblue' : null;
	}



	openTableDialog(scheduledSubmissions) {
		this.displayTableDialog = true;
	}

	countRuns(date, process){

		let result;
		result = this.data4count.filter((p: any) => (p.mdate.getDate() == date.getDate() && p.mdate.getMonth() == date.getMonth()) && (p.process == process));
		return result.length
	}

}
