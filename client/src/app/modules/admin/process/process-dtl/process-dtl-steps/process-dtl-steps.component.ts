import { Component, OnInit, Input } from '@angular/core';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder } from '@angular/forms';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
	selector: 'app-process-dtl-steps',
	templateUrl: './process-dtl-steps.component.html',
	styleUrls: [ './process-dtl-steps.component.scss' ]
})
export class ProcessDtlStepsComponent implements OnInit {
	@Input() isVisible: boolean = true;

	stepName = '';
	stepId = -1;
	required = false;
	parallel = false;
	disabled = false;
	selectedValues: boolean;
	processStepForm: FormGroup;
	processSteps = [];
	manualduration: number;

	columns = [
		{ field: 'name', header: 'name' },
		{ field: 'required', header: 'required' },
		{ field: 'durationDisplay', header: 'calculated/manual duration(mins)' },
		{ field: 'parallel', header: 'parallel' },
		{ field: 'disabled', header: 'disabled' }
	];
	processId;
	constructor(
		private processSvc: ProcessService,
		private msgSvc: MessageService,
		private route: ActivatedRoute,
		private fb: FormBuilder,
		private sessionService: SessionService
	) {
		this.setupForm();
	}

	get isAdmin() {
		return this.sessionService.role === 'admin';
	}

	get canEdit(): boolean {
		return this.sessionService.isUserOfProcess(this.processId);
	}

	messageAlert() {
		if (this.selectedValues) {
			const step = this.processSteps.find((x) => x.id === this.stepId);
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `Step '${step.name}' is disabled.No New Submissions Will Be Entertained For The Disabled Step.`
			});
		}
	}

	nameAlert() {
		if (this.stepId > 0) {
			const step = this.processSteps.find((x) => x.id === this.stepId);
			if (step.name !== this.processStepForm.value.name) {
				this.msgSvc.add({
					severity: 'error',
					summary: 'Step Name Changed!',
					detail: `Step '${step.name}' changed to '${this.processStepForm.value
						.name}'.All prior submission with the step name will change.`
				});
			}
		}
	}

	ngOnInit() {
		this.route.parent.params.subscribe(params => {
			this.processId = params['id'] || 0;
			if (this.processId != 0) {
				this.getProcessSteps(this.processId);
			}
		});
	}

	newNotification() {
		this.stepId = 0;
		this.setupForm();
	}

	setupForm() {
		this.processStepForm = this.fb.group({
			name: '',
			duration: '',
			required: false,
			parallel: false,
			disabled: false
		});
	}

	cancel() {
		this.stepId = -1;
	}

	onSubmit() {
		const step = this.processSteps.find((x) => x.id === this.stepId);
		if (this.stepId === 0) {
			this.processSvc.newProcessStep(this.processId, this.processStepForm.value).subscribe((value) => {
				this.msgSvc.add({
					severity: 'success',
					summary: 'New step in town!',
					detail: `Step '${value.name}' added`
				});
				this.getProcessSteps(this.processId);
				this.setupForm();
				this.stepId = -1;
			});
		} else {
			this.processSvc
				.updateProcessStep(this.processId, this.stepId, this.processStepForm.value)
				.subscribe((value) => {
					this.msgSvc.add({
						severity: 'success',
						summary: 'New step in town!',
						detail: `Step '${value.name}' added`
					});
					this.getProcessSteps(this.processId);
					this.setupForm();
					this.stepId = -1;
				});
		}
	}

	getProcessSteps(id: number) {
		this.processSvc.getAllProcessSteps(id).subscribe((value: any[]) => {
			const endStep = value.splice(1, 1);
			value.push(endStep[0]);
			this.processSteps = value;
			this.processSteps.forEach((p) => {
				p.durationDisplay = p.manualDuration ? `${p.duration}/${p.manualDuration}` : `${p.duration}`;
			});
		});
	}

	onDeleteRecord(id) {
		this.processSvc.deleteProcessStep(this.processId, id).subscribe((value) => {
			this.processSteps = this.processSteps.filter((p) => {
				return p.id !== id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `Step was removed from process`
			});
		});
	}

	onEditRecord(id) {
		this.stepId = Number.parseInt(id);
		const step = this.processSteps.find((x) => x.id === this.stepId);
		const duration = step.manualDuration === null ? step.duration : step.manualDuration;

		this.processStepForm.patchValue({
			required: step.required,
			parallel: step.parallel,
			duration: duration,
			disabled: step.disabled,
			name: step.name
		});
	}
}
