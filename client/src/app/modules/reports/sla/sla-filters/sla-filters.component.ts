import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ProcessService } from 'src/app/modules/admin/process/process.service';

@Component({
	selector: 'app-sla-filters',
	templateUrl: './sla-filters.component.html',
	styleUrls: ['./sla-filters.component.scss'],
})
export class SlaFiltersComponent implements OnInit {
	@Output() onChangeFilters = new EventEmitter();

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
	filterForm: FormGroup;
	processes = [];
	parentProcesses = [];
	dateOption = 'last30Days';
	processOption = 'child';

	constructor(private fb: FormBuilder, private processService: ProcessService) {
		this.setupForm();
	}

	ngOnInit(): void {
		this.getProcesses();
	}

	resetFilters() {
		this.onChangeFilters.emit(null);
		this.setupForm();
		this.dateOption = 'last30Days';
		this.processOption = 'child';
		this.onChangeDateOption('last30Days');
	}

	onBlurComponent() {
		this.filterForm.get('dateOption').setValue(this.dateOption);
		this.filterForm.get('processOption').setValue(this.processOption);
		if (!this.filterForm.valid) return;
		const filters: any = {};
		const formValue = this.filterForm.value;
		if (formValue.dateOption === 'period' && formValue.period) {
			filters.periodTo = formValue.period;
		} else if (formValue.dateRange) {
			filters.from = formValue.dateRange[0].toISOString().split('T')[0];
			filters.to = formValue.dateRange[1].toISOString().split('T')[0];
		}

		if (formValue.processOption === 'child') {
			filters.childId = '';
			filters.parentId = '';
			filters.childId += formValue.process.id + ',';
			filters.parentId += formValue.process.processParent ? formValue.process.processParent.id + ',' : '';
		} else if (formValue.parent) {
			filters.childId = '';
			filters.parentId = '';
			filters.parentId += formValue.parent.id + ',';
			this.processes.forEach((pr) => {
				if (pr.processParent && pr.processParent.id === formValue.parent.id) {
					filters.childId += pr.id + ',';
				}
			});
		}
		if (formValue.bu) {
			filters.bu = formValue.bu;
		}
		filters.size = 30;
		filters.schedOnly = 'true';
		filters.sortOrder = '-1';
		this.onChangeFilters.emit(filters);
	}

	getProcesses() {
		this.processService.getAllProcesses(true).subscribe((value: any[]) => {
			this.processes = value.filter((x) => !x.isParent);
			this.parentProcesses = value.filter((x) => x.isParent);
		});
	}

	setupForm() {
		this.filterForm = this.fb.group({
			dateRange: null,
			period: [{ value: null, disabled: true }],
			dateOption: 'last30Days',
			process: [{ value: null, disabled: false }, Validators.required],
			parent: [{ value: null, disabled: true }, Validators.required],
			processOption: 'child',
			bu: null,
		});
		this.onChangeForm();
		this.onChangeDateOption('last30Days');
	}

	onChangeForm() {
		this.filterForm.valueChanges.subscribe();
	}

	onChangeProcessOption(value) {
		if (value === 'child') {
			this.filterForm.get('process').enable();
			this.filterForm.get('parent').disable();
		} else {
			this.filterForm.get('parent').enable();
			this.filterForm.get('process').disable();
		}
	}

	onChangeDateOption(value) {
		switch (value) {
			case 'dateRange':
				this.filterForm.get('dateRange').enable();
				this.filterForm.get('period').disable();
				break;
			case 'period':
				this.filterForm.get('dateRange').disable();
				this.filterForm.get('period').enable();
				break;
			case 'last30Days':
				const today = new Date();
				const todayMinus30Days = new Date();
				todayMinus30Days.setDate(today.getDate() - 30);
				this.filterForm.get('dateRange').setValue([todayMinus30Days, today]);
				this.filterForm.get('dateRange').enable();
				this.filterForm.get('period').disable();
				break;
		}
	}

	get process() {
		return this.filterForm.get('process');
	}

	get parent() {
		return this.filterForm.get('parent');
	}
}
