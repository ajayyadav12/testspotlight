import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RelationshipService } from '../relationship.service';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { GoogleCharts } from 'google-charts';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { DatePipe } from '@angular/common';
import * as moment from 'moment-timezone';
import { Colors } from 'src/app/shared/Constants/Colors';

declare var vis: any;
@Component({
	selector: 'app-relationship-processes',
	templateUrl: './relationship-processes.component.html',
	styleUrls: ['./relationship-processes.component.scss']
})
export class RelationshipProcessesComponent implements OnInit {
	/**
   * [0]: System ID
   * [1]: System name
   * [2]: Direction ('S', 0, 'R')
   * [3]: Status and SubmissionID (ex. '3,200')
   * [4]: Process Name
   * [5]: Is parent
   */
	systems = [[]];
	systemId;
	showTimeline = false;
	submission: any = {
		submissionId: 0,
		isParent: false
	};
	timeOption = '1';
	avgRunTime = '0.00';
	avgRecordCount = '0.00';
	avgWarning = '0.00';
	avgError = '0.00';
	showAverage = false;

	constructor(
		private route: ActivatedRoute,
		private relationshipSvc: RelationshipService,
		private submissionSvc: SubmissionsService,
		private datePipe: DatePipe
	) { }

	ngOnInit() {
		this.systemId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
		this.buildSystemRelationship();
	}

	buildSystemRelationship() {
		this.relationshipSvc.getSystemRelationships(this.systemId, this.timeOption).subscribe(value => {
			this.systems = value;
			this.createNetwork(value);
		});
	}

	/**
   * Create network viz based on system dataset
   * @param systems
   */
	createNetwork(systems: any[]) {
		const systemNodes = [];
		const nodeEdges = [];
		const primarySystemIndex = systems.findIndex(s => s[2] === '0');

		// Create Nodes
		systems.forEach((currentSystem, index) => {
			let fromIndex = -1;
			let toIndex = -1;
			const n: any = {
				id: currentSystem[0],
				label: currentSystem[1].replace(/ /g, '\n'),
				destination: currentSystem[2],
				group: 'center',
				x: 0,
				y: 0
			};

			const isParent = currentSystem[5] === 'Y' ? true : false;
			const statusId = currentSystem[3] ? Number.parseInt(currentSystem[3].split(',')[0]) : null;
			const submissionId = currentSystem[3] ? Number.parseInt(currentSystem[3].split(',')[1]) : null;
			const statusColor = this.getColor(statusId);
			const existingSystem = systemNodes.find(node => node.id === currentSystem[0]);

			// Create edges to and from system
			if (index < primarySystemIndex) {
				fromIndex = existingSystem ? existingSystem.id : currentSystem[0];
				toIndex = systems[primarySystemIndex][0];
				n.x = -300;
				n.y = index * 30;
				n.group = 'sender';
			} else if (index > primarySystemIndex) {
				fromIndex = systems[primarySystemIndex][0];
				toIndex = existingSystem ? existingSystem.id : currentSystem[0];
				n.group = 'receiver';
				n.x = 300;
				n.y = index * 30;
			}

			// If there's an existing node with the same system id, don't create a new node just a new edge
			if (!existingSystem) {
				systemNodes.push(n);
			}

			nodeEdges.push({
				from: fromIndex,
				to: toIndex,
				arrows: 'to',
				background: {
					enabled: true,
					color: statusColor,
					size: 10
				},
				label: currentSystem[4],
				font: { align: 'top', size: 12 },
				title: submissionId
					? `Click to show ${isParent ? 'Parent' : ''} Submission #${submissionId} details`
					: 'Submission not available'
			});
		});

		const nodes = new vis.DataSet(systemNodes);
		const edges = new vis.DataSet(nodeEdges);

		const container = document.getElementById('network');

		const data = {
			nodes: nodes,
			edges: edges
		};

		const options = {
			nodes: {
				shape: 'dot',
				size: 35,
				font: {
					size: 14
				},
				margin: 20,
				borderWidth: 2,
				shadow: true
			},
			edges: {
				width: 2
			},
			interaction: { hover: true },
			layout: { randomSeed: 1 },
			physics: {
				repulsion: {
					nodeDistance: 100,
					springLength: 280,
					damping: 1,
					springConstant: 0
				},
				solver: 'repulsion'
			},
			groups: {
				receiver: {
					shape: 'circle',
					color: '#d1e4fd'
				},
				sender: {
					shape: 'box',
					color: '#d1e4fd'
				},
				center: {
					shape: 'database',
					color: '#95c3ff'
				}
			}
		};

		const network = new vis.Network(container, data, options);

		network.on('click', params => {
			this.submission.submissionId = 0;
			this.submission.isParent = false;
			this.showAverage = false;
			setTimeout(_ => {
				// Look for the process in systems dataset to get the latest submission Id
				const process = this.systems.find(x => x[4] === network.body.edges[params.edges[0]].options.label);
				this.submission.submissionId = process[3] ? Number.parseInt(process[3].split(',')[1]) : 0;
				this.submission.isParent = process[5] === 'Y' ? true : false;
				if (this.submission.isParent) {
					this.submissionSvc.getSubmissionParent(this.submission.submissionId).subscribe(value => {
						GoogleCharts.load(
							_ => {
								this.drawTimelineChart(value.children);
							},
							{ packages: ['timeline'] }
						);
					});

					this.relationshipSvc.getParentSubmission(this.submission.submissionId).subscribe(value => {
						moment.tz.setDefault('America/New_York');
						let startMoment = moment(value.startTime);
						let endMoment = moment(value.endTime);

						value.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
						value.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');

						this.submission.startTime = this.datePipe.transform(new Date(value.startTime), 'medium');
						this.submission.endTime = value.endTime
							? this.datePipe.transform(new Date(value.endTime), 'mediumTime')
							: 'Running...';
						this.submission.processName = value.process.name;
					});
				} else {
					this.relationshipSvc.getSubmission(this.submission.submissionId).subscribe(value => {
						moment.tz.setDefault('America/New_York');
						let startMoment = moment(value.startTime);
						let endMoment = moment(value.endTime);

						value.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
						value.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');

						this.submission.startTime = this.datePipe.transform(new Date(value.startTime), 'medium');
						this.submission.endTime = value.endTime
							? this.datePipe.transform(new Date(value.endTime), 'mediumTime')
							: 'Running...';
						this.submission.processName = value.process.name;
						this.submission.notes = value.notes;
						this.submission.scheduledSubmission = value.scheduledSubmission;
						if (this.submission.scheduledSubmission) {
							this.submission.scheduledSubmission.startTime = this.datePipe.transform(
								new Date(this.submission.scheduledSubmission.startTime),
								'medium'
							);
							this.submission.scheduledSubmission.endTime = this.datePipe.transform(
								new Date(this.submission.scheduledSubmission.endTime),
								'mediumTime'
							);
						}
					});
					this.getAverageValues();

				}
				this.showTimeline = true;
			}, 200);
		});
	}

	getAverageValues() {
		this.showAverage = true;
		this.relationshipSvc.getAverageValue(this.submission.submissionId).subscribe(avgValue => {
			this.avgRunTime = '0.00';
			if (avgValue[0] > 0) {
				this.avgRunTime = avgValue[0];
			}

			this.avgWarning = '0.00';
			if (avgValue[1] > 0) {
				this.avgWarning = avgValue[1];
			}

			this.avgError = '0.00';

			if (avgValue[2] > 0) {
				this.avgError = avgValue[2];
			}

			this.avgRecordCount = '0.00';

			if (avgValue[3] > 0) {
				this.avgRecordCount = avgValue[3];
			}


		});
	}


	/**
   * Return blue, orange, green or red depending on status id
   * @param statusId 1-4 status ids
   */
	getColor(statusId): string {
		let color = Colors.lightgray;
		switch (statusId) {
			case 1:
				color = Colors.blue + '9b';
				break;
			case 2:
				color = Colors.green + '9b';
				break;
			case 3:
				color = Colors.yellow + '9b';
				break;
			case 4:
				color = Colors.red + '9b';
				break;
		}

		return color;
	}

	onHideTimelinePanel() {
		this.submission.submissionId = 0;
	}

	drawTimelineChart(submissions: any[]) {
		const container = document.getElementById('timeline_chart');
		const chart = new GoogleCharts.api.visualization.Timeline(container);
		const dataTable = new GoogleCharts.api.visualization.DataTable();
		let chartHeight = 350;
		let endTime;

		dataTable.addColumn({ type: 'string', id: 'Step' });
		dataTable.addColumn({ type: 'string', id: 'dummy bar label' });
		dataTable.addColumn({ type: 'string', id: 'style', role: 'style' });
		dataTable.addColumn({ type: 'date', id: 'Start' });
		dataTable.addColumn({ type: 'date', id: 'End' });

		submissions.forEach(submission => {
			const startTime = new Date(submission.startTime);
			let iconNotes = '';
			let iconColor = '';

			if (submission.notes != null) {
				iconColor = '; stroke-color: #085402';
				iconNotes = ' *';
			}

			if (submission.endTime != null) {
				endTime = new Date(submission.endTime);
			} else {
				endTime = new Date();
				submission.duration = submission.duration > 0 ? submission.duration : 1;
			}

			const diff = endTime.getTime() - startTime.getTime();

			if (diff === 0) {
				endTime.setTime(endTime.getTime() + 1000);
			}

			dataTable.addRows([
				[
					`#${submission.id} - ${submission.process.name}`,
					submission.status.name + iconNotes,
					'color: ' + SubmissionCommon.submissionStatusColor(submission.status.name) + iconColor,
					startTime,
					endTime
				]
			]);
		});

		if (submissions.length === 1) {
			chartHeight = 130;
		} else if (submissions.length <= 5) {
			chartHeight = submissions.length * 65;
		}

		const options = {
			height: chartHeight,
			timeline: {
				rowLabelStyle: { fontSize: 14 }
			}
		};

		chart.draw(dataTable, options);
	}

	onFocusTimeFilter(event) {
		this.buildSystemRelationship();
	}
}
