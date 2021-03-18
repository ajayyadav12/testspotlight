import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { GEFiltersService } from './ge-filters.service';
import { GEFilters } from './GEFilters';
import { SelectItem } from 'primeng/api/selectitem';
import { nullSafeIsEquivalent } from '@angular/compiler/src/output/output_ast';

@Component({
	selector: 'app-ge-filters',
	templateUrl: './ge-filters.component.html',
	styleUrls: ['./ge-filters.component.scss'],
	providers: [GEFiltersService],
})
export class GEFiltersComponent implements OnInit {
	hide = false;
	daysOptions = [
		{ label: 'Last 7 days', value: 7 },
		{ label: 'Last 14 days', value: 14 },
		{ label: 'Last 30 days', value: 30 },
		{ label: 'Last 90 days', value: 90 },
	];
	fieldOptions: SelectItem[] = [
		{ label: 'Search By', value: 'searchBy' },
		{ label: 'Period From', value: 'periodFrom' },
		{ label: 'Period To', value: 'periodTo' },
		{ label: 'Submission ID', value: 'id' },
		{ label: 'Date Range', value: 'dateRange' },
		{ label: 'Process', value: 'process' },
		{ label: 'Parent', value: 'parent' },
		{ label: 'Sender', value: 'sender' },
		{ label: 'Receiver', value: 'receiver' },
		{ label: 'Status', value: 'status' },
		{ label: 'Business Unit', value: 'bu' },
		{ label: 'Alt ID', value: 'altId' },
		{ label: 'Notes', value: 'notes' },
		{ label: 'Ad Hoc', value: 'adHoc' },
		{ label: 'Duration', value: 'duration' },
	];
	@Input()
	filterSelection = [
		'searchBy',
		'periodFrom',
		'periodTo',
		'dateRange',
		'days',
		'id',
		'process',
		'parent',
		'sender',
		'receiver',
		'status',
		'bu',
		'altId',
		'notes',
		'adHoc',
		'duration',
		'durationTime',
	];
	@Input() cloumnFilters;
	@Input() childParentRelationship = true;
	@Output() clickFilter = new EventEmitter();
	submissionFilterForm: FormGroup;
	periodTooltipText = `Please enter a valid format (Hint: Quarterly : Q20-(1-4), Yearly
              : Y2020
              ,Monthly :
              M20-03,Monthly : MMM-YY)`;

	processes = [];
	parentProcesses = [];
	rangeEnd;
	rangeStart;
	val2: number;
	isPeriod: boolean = false;
	systems = [];
	status = [
		{ label: 'In Progress', value: 1 },
		{ label: 'Success', value: 2 },
		{ label: 'Warning', value: 3 },
		{ label: 'Failed', value: 4 },
		{ label: 'Long Running', value: 6 },
	];
	timeDuration = [
		{ label: 'Greater Than Or Equal To', value: 'G' },
		{ label: 'Less Than', value: 'L' },
	];
	businesses = [
		{ label: 'Aviation', value: 'AV' },
		{ label: 'Corporate', value: 'CO' },
		{ label: 'Healthcare', value: 'HC' },
		{ label: 'Oil & Gas', value: 'OG' },
		{ label: 'Renewables', value: 'RE' },
		{ label: 'Total Company', value: 'TC' },
		{ label: 'Capital', value: 'CA' },
		{ label: 'Power', value: 'PO' },
		{ label: 'Industrial', value: 'IN' },
		{ label: 'GECSIN', value: 'GN' },
		{ label: 'GPINP', value: 'GP' },
	];

	datePattern =
		'(Q[0-9][0-9]-[1-4])|(Y[0-9][0-9][0-9][0-9])|(M[0-9][0-9]-[0-1][0-9])|(((JAN)|(FEB)|(MAR)|(APR)|(MAY)|(JUN)|(JUL)|(AUG)|(SEP)|(OCT)|(NOV)|(DEC))-[2-2][0-9])';

	adHoc = [
		{ label: 'Scheduled Only', value: 'N' },
		{ label: 'Adhoc Only', value: 'Y' },
	];

	get input() {
		return this.submissionFilterForm ? this.submissionFilterForm.getRawValue() : null;
	}

	constructor(
		private fb: FormBuilder,
		private submissionsFilterSvc: GEFiltersService,
		private route: Router,
		private activatedRoute: ActivatedRoute
	) {
		this.reset();
	}

	ngOnInit() {}

	checkPattern() {
		let re = new RegExp(this.datePattern);

		if (
			this.submissionFilterForm.controls['periodFrom'].value !== null &&
			this.submissionFilterForm.controls['periodFrom'].value !== '' &&
			!this.submissionFilterForm.controls['periodFrom'].value.match(re)
		) {
			alert('Please Enter A Valid Date Format.');
		} else if (
			this.submissionFilterForm.controls['periodTo'].value !== null &&
			this.submissionFilterForm.controls['periodTo'].value !== '' &&
			!this.submissionFilterForm.controls['periodTo'].value.match(re)
		) {
			alert('Please Enter A Valid Date Format.');
		} else {
			this.filter();
		}
	}

	disablePeriod() {
		if (this.submissionFilterForm.controls['searchBy'].value === 'datePeriod') {
			this.isPeriod = true;
			this.submissionFilterForm.controls['dateRange'].setValue(null);
		} else {
			this.isPeriod = false;
			this.submissionFilterForm.controls['periodFrom'].setValue(null);
			this.submissionFilterForm.controls['periodTo'].setValue(null);
		}
	}

	filter() {
		const queryParams: GEFilters = {
			days: 7,
			from: null,
			to: null,
			dateRange: null,
			periodFrom: null,
			periodTo: null,
			level: '',
			childId: '',
			parentId: '',
			sender: '',
			receiver: '',
			status: '',
			bu: '',
			altId: '',
			notes: '',
			searchBy: '',
			adHoc: null,
			duration: '',
			durationTime: '',
			id: '',
		};

		if (
			this.submissionFilterForm.value.searchBy != null &&
			this.submissionFilterForm.value.searchBy === 'datePeriod'
		) {
			const fromDate = this.submissionFilterForm.value.periodFrom.toUpperCase();
			const toDate = this.submissionFilterForm.value.periodTo.toUpperCase();

			let startDate;
			let endDate;
			let lastDay;

			if (fromDate && toDate !== null) {
				switch (fromDate.substring(0, 1)) {
					case 'Q':
						startDate = new Date(
							2000 + Number(fromDate.substring(1, 3)),
							Number(fromDate.substring(4, 5)) * 3 - 3,
							1
						);
						endDate = new Date(
							2000 + Number(toDate.substring(1, 3)),
							Number(toDate.substring(4, 5)) * 3,
							0
						);

						break;

					case 'Y':
						startDate = '1/1/' + fromDate.substring(1, 5);
						endDate = '12/31/' + toDate.substring(1, 5);

						break;
					case fromDate.substring(0, 1) === 'M' &&
						fromDate.substring(0, 3) !== 'MAR' &&
						fromDate.substring(0, 3) !== 'MAY':
						startDate = new Date(2000 + Number(fromDate.substring(1, 3)), fromDate.substring(4, 6));
						lastDay = new Date(2000 + Number(toDate.substring(1, 3)), toDate.substring(4, 6));
						endDate = new Date(lastDay.getFullYear(), lastDay.getMonth() + 1, 0);

						break;
					default:
						startDate = new Date('1-' + fromDate);
						lastDay = new Date('1-' + toDate);
						endDate = new Date(lastDay.getFullYear(), lastDay.getMonth() + 1, 0);
				}

				this.rangeStart = new Date(new Date(startDate).setHours(0, 0, 0)).toISOString().split('T')[0];
				this.rangeEnd = new Date(new Date(endDate).setHours(23, 59, 59)).toISOString().split('T')[0];
			}
		}
		// startDate
		if (this.submissionFilterForm.value.dateRange != null && this.submissionFilterForm.value.dateRange[0]) {
			var newFromDate = new Date(this.submissionFilterForm.value.dateRange[0]);
			queryParams.from = new Date(newFromDate.setDate(newFromDate.getDate() + 1)).toISOString().split('T')[0];
			//queryParams.from = new Date(this.submissionFilterForm.value.dateRange[0]).toISOString().split('T')[0];
		} else if (this.submissionFilterForm.value.periodFrom) {
			queryParams.from = this.rangeStart;
		} else {
			queryParams.from = null;
		}
		if (queryParams.from) {
		}
		// endDate
		if (this.submissionFilterForm.value.dateRange != null && this.submissionFilterForm.value.dateRange[1]) {
			var newToDate = new Date(this.submissionFilterForm.value.dateRange[1]);
			//queryParams.to = new Date(this.submissionFilterForm.value.dateRange[1]).toISOString().split('T')[0];
			queryParams.to = new Date(newToDate.setDate(newToDate.getDate() + 1)).toISOString().split('T')[0];
		} else if (this.submissionFilterForm.value.periodTo) {
			queryParams.to = this.rangeEnd;
		} else {
			queryParams.to = null;
		}
		if (queryParams.to) {
		}
		queryParams.level = this.submissionFilterForm.value.level;
		// level: process, parent, sender/receiver
		switch (this.submissionFilterForm.value.level) {
			case 'PR': // childId, parentId
				this.submissionFilterForm.value.processes.forEach((pr) => {
					queryParams.childId += pr.id + ',';
					queryParams.parentId += pr.processParent ? pr.processParent.id + ',' : '';
				});

				queryParams.childId = queryParams.childId === '' ? null : queryParams.childId;
				if (this.childParentRelationship) {
					queryParams.parentId =
						queryParams.parentId === ''
							? queryParams.childId === null
								? null
								: '0'
							: queryParams.parentId;
				} else {
					queryParams.parentId = null;
				}

				break;

			case 'PA': // parentId, childId
				this.submissionFilterForm.value.parents.forEach((pa) => {
					queryParams.parentId += pa.id + ',';

					this.processes.forEach((pr) => {
						if (pr.processParent && pr.processParent.id === pa.id) {
							queryParams.childId += pr.id + ',';
						}
					});
				});

				queryParams.parentId = queryParams.parentId === '' ? null : queryParams.parentId;
				if (this.childParentRelationship) {
					queryParams.childId =
						queryParams.childId === '' ? (queryParams.parentId === null ? null : '0') : queryParams.childId;
				} else {
					queryParams.childId = null;
				}

				break;

			case 'SR': // sender, receiver
				this.submissionFilterForm.value.senders.forEach((s) => {
					queryParams.sender += s.id + ',';
				});
				this.submissionFilterForm.value.receivers.forEach((r) => {
					queryParams.receiver += r.id + ',';
				});

				queryParams.childId = null;
				queryParams.parentId = null;
				break;
		}

		queryParams.sender = queryParams.sender === '' ? null : queryParams.sender;
		queryParams.receiver = queryParams.receiver === '' ? null : queryParams.receiver;

		// status
		this.submissionFilterForm.value.status.forEach((s) => {
			queryParams.status += s + ',';
		});
		queryParams.status = queryParams.status === '' ? null : queryParams.status;

		queryParams.days = this.submissionFilterForm.value.days;

		// bu
		queryParams.bu = this.submissionFilterForm.value.bu;
		if (queryParams.bu) {
		}

		// altId
		queryParams.altId = this.submissionFilterForm.value.altId;
		if (queryParams.altId) {
		}

		// notes
		queryParams.notes = this.submissionFilterForm.value.notes;

		// searchBy
		queryParams.searchBy = this.submissionFilterForm.value.searchBy;

		// periodFrom
		queryParams.periodFrom = this.submissionFilterForm.value.periodFrom;

		// periodTo
		queryParams.periodTo = this.submissionFilterForm.value.periodTo;

		// duration
		queryParams.duration = this.submissionFilterForm.value.duration;

		// durationTime
		queryParams.durationTime = this.submissionFilterForm.value.durationTime;

		// adHoc
		queryParams.adHoc = this.submissionFilterForm.value.adHoc;
		if (queryParams.adHoc) {
		}

		// submissionId
		queryParams.id = this.submissionFilterForm.value.id;
		if (queryParams.id) {
		}

		this.route.navigate([], {
			queryParams: queryParams,
			relativeTo: this.activatedRoute,
			queryParamsHandling: 'merge',
		});
	}

	onClickClear() {
		this.submissionFilterForm.setValue({
			days: 7,
			dateRange: null,
			periodFrom: null,
			periodTo: null,
			level: 'PR',
			processes: [],
			parents: [],
			receivers: [],
			senders: [],
			status: [],
			bu: null,
			altId: null,
			notes: null,
			searchBy: 'dateRange',
			adHoc: null,
			id: null,
			duration: null,
			durationTime: null,
		});
		this.isPeriod = false;
		this.filter();
	}

	reset() {
		this.submissionFilterForm = this.fb.group({
			days: 7,
			dateRange: null,
			periodFrom: null,
			periodTo: null,
			level: 'PR',
			processes: [[]],
			parents: [[]],
			receivers: [[]],
			senders: [[]],
			status: [[]],
			bu: null,
			altId: null,
			notes: null,
			searchBy: 'dateRange',
			adHoc: null,
			id: null,
			duration: null,
			durationTime: null,
		});
		this.isPeriod = false;
	}

	fixValues(value) {
		switch (value) {
			case 'PR':
				this.submissionFilterForm.get('parents').setValue([]);
				this.submissionFilterForm.get('receivers').setValue([]);
				this.submissionFilterForm.get('senders').setValue([]);
				this.submissionFilterForm.get('level').setValue('PR');
				break;
			case 'PA':
				this.submissionFilterForm.get('processes').setValue([]);
				this.submissionFilterForm.get('receivers').setValue([]);
				this.submissionFilterForm.get('senders').setValue([]);
				this.submissionFilterForm.get('level').setValue('PA');
				break;
			case 'SR':
				this.submissionFilterForm.get('processes').setValue([]);
				this.submissionFilterForm.get('parents').setValue([]);
				this.submissionFilterForm.get('level').setValue('SR');
				break;
			default:
				break;
		}
	}

	getSender() {
		this.submissionsFilterSvc.getAllSenders().subscribe((value) => {
			this.systems = value;
		});
	}

	getProcesses() {
		this.submissionsFilterSvc.getAllProcesses(true).subscribe((value: any[]) => {
			this.processes = value.filter((x) => !x.isParent);
			this.parentProcesses = value.filter((x) => x.isParent);
		});
	}

	showFilter(filterName: string): boolean {
		return this.filterSelection.includes(filterName);
	}

	/**
	 * Before displaying filter panel, update form value with query params.
	 */
	updateFilters() {
		if (this.processes.length === 0) {
			this.submissionsFilterSvc.getAllProcesses(true).subscribe((value: any[]) => {
				this.processes = value.filter((x) => !x.isParent);
				this.parentProcesses = value.filter((x) => x.isParent);
				this._updateFilters();
			});
		} else {
			this._updateFilters();
		}
	}

	_updateFilters() {
		const queryParams = this.activatedRoute.snapshot.queryParams;
		let processes = [];
		let parents = [];
		let receivers = [];
		let senders = [];
		let status = [];
		let columnFilter = [];

		if (queryParams.childId && queryParams.level === 'PR') {
			processes = queryParams.childId.split(',').map((p: string) => {
				const process = this.processes.find((x) => x.id === Number.parseInt(p));
				return process;
			});
			processes.pop();
		}
		if (queryParams.parentId && queryParams.level === 'PA') {
			parents = queryParams.parentId.split(',').map((p: string) => {
				const parent = this.parentProcesses.find((x) => x.id === Number.parseInt(p));
				return parent;
			});
			parents.pop();
		}
		if (queryParams.receiver) {
			receivers = queryParams.receiver.split(',').map((p: string) => {
				const receiver = this.systems.find((x) => x.id === Number.parseInt(p));
				return receiver;
			});
			receivers.pop();
		}
		if (queryParams.sender) {
			senders = queryParams.sender.split(',').map((p: string) => {
				const sender = this.systems.find((x) => x.id === Number.parseInt(p));
				return sender;
			});
			senders.pop();
		}
		if (queryParams.status) {
			status = queryParams.status.split(',').map((p: string) => {
				if (p !== '') {
					const status = this.status.find((x) => x.value === Number.parseInt(p));
					return status.value;
				}
			});
			status.pop();
		}

		this.isPeriod = queryParams.searchBy === undefined ? false : true;

		this.submissionFilterForm.setValue({
			periodFrom: queryParams.periodFrom ? queryParams.periodFrom : null,
			periodTo: queryParams.periodTo ? queryParams.periodTo : null,
			from: queryParams.from ? new Date(queryParams.from) : null,
			to: queryParams.to ? new Date(queryParams.to) : null,
			id: queryParams.id ? queryParams.id : null,
			days: queryParams.days ? queryParams.days : null,
			dateRange: queryParams.dateRange ? queryParams.dateRange : null,
			level: queryParams.level ? queryParams.level : 'PR',
			processes: processes ? processes : [],
			parents: parents ? parents : [],
			receivers: receivers ? receivers : [],
			senders: senders ? senders : [],
			status: status ? status : [],
			bu: queryParams.bu ? queryParams.bu : null,
			altId: queryParams.altId ? queryParams.altId : null,
			notes: queryParams.notes ? queryParams.notes : null,
			adHoc: queryParams.adHoc ? queryParams.adHoc : null,
			duration: queryParams.duration ? queryParams.duration : null,
			durationTime: queryParams.durationTime ? queryParams.durationTime : null,
			searchBy: queryParams.searchBy ? queryParams.searchBy : 'dateRange',
		});
	}
}
