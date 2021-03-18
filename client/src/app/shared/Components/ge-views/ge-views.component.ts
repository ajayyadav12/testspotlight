import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { GEChipFilter } from 'src/app/modules/dashboard/dashboard-filters/GEChipFilter';
import { ViewsService } from './views.service';

@Component({
	selector: 'app-ge-views',
	templateUrl: './ge-views.component.html',
	providers: [ViewsService],
})
export class GEViewsComponent implements OnInit {
	@Input() tParams;
	@Input() moduleName: string;
	@Input() navigate: string;
	@Input() chipFilters: GEChipFilter[];
	@Input() columnFilters: any[];
	@Output() updateColumnsSelection = new EventEmitter();

	columnFilter = [
		'id',
		'process',
		'stat',
		'startTime',
		'endTime',
		'totalTime',
		'records',
		'warnings',
		'errors',
		'period',
		'adHoc',
		'parent',
		'action',
	];

	@Input()
	set _view(view: any) {
		this.view = view;
	}

	views = [];
	newViewName = '';
	showNewViewName = false;
	view;

	constructor(private viewSvc: ViewsService, private msgSvc: MessageService, private router: Router) {}

	ngOnInit() {
		this.getViews();
	}

	get showStar(): boolean {
		return this.view;
	}

	get starTooltip(): string {
		return this.view.default ? 'Unset default' : 'Set default';
	}

	get showTrash(): boolean {
		return this.view;
	}

	get starIcon(): string {
		return this.view.default ? 'pi pi-star' : 'pi pi-star-o';
	}

	transformView(view) {
		const settings = JSON.parse(view.settings);
		view.default = settings['default'] || false;
		delete settings['default'];
		view.settings = settings;
		return view;
	}

	setViews(value, lastId) {
		this.views = value.filter((val) => val.settings).map(this.transformView);
		// look for last id
		this.view = this.views.find((view) => view.id === lastId);
		if (!this.view) {
			// look for default
			this.view = this.views.find((view) => view.default);
		}
		this.onSelectView(this.view);
	}

	/**
	 * Get user views, select latest view and update queries
	 */
	getViews() {
		this.viewSvc.getViews(this.moduleName).subscribe((value) => {
			let lastId = 0;
			const lastView = localStorage.getItem(`${this.moduleName}-view`);
			if (lastView) {
				lastId = JSON.parse(lastView).id;
			}
			this.setViews(value, lastId);
		});
	}

	/**
	 * Update QueryParams and Latest view in local storage
	 * @param value Unused
	 */
	onSelectView(event) {
		const tValue = this.view ? this.view.settings : null;
		if (tValue) {
			localStorage.setItem(`${this.moduleName}-chip-filters`, this.view.chipFilters);
			localStorage.setItem(`${this.moduleName}-view`, JSON.stringify(this.view));
		} else {
			localStorage.removeItem(this.moduleName + '-view');
			localStorage.removeItem(this.moduleName + '-chip-filters');
		}
		if (this.navigate) {
			this.router.navigate([this.navigate], { queryParams: tValue });
		}
		if (event) {
			if (event.value === null || event.value.columnFilters === '[]' || event.value.columnFilters === undefined) {
				this.updateColumnsSelection.emit(this.columnFilter);
			} else {
				this.updateColumnsSelection.emit(JSON.parse(event.value.columnFilters));
			}
		}
	}

	showNewViewInput() {
		this.newViewName = '';
		this.showNewViewName = true;
	}

	onSaveView() {
		const chipFilters = localStorage.getItem(`${this.moduleName}-chip-filters`);
		this.viewSvc
			.saveView({
				name: this.newViewName,
				moduleName: this.moduleName,
				settings: JSON.stringify(this.tParams),
				chipFilters: chipFilters,
				columnFilters: JSON.stringify(this.columnFilters),
			})
			.subscribe((value) => {
				const view = this.transformView(value);
				this.showNewViewName = false;
				this.newViewName = '';
				this.views.push(view);
				this.view = view;
				this.msgSvc.add({
					severity: 'success',
					summary: `All set!`,
					detail: `New custom view added`,
				});
				localStorage.setItem(this.moduleName + '-view', JSON.stringify(value));
			});
	}

	onStarView() {
		this.views.forEach((view) => {
			view.settings['default'] = view.id === this.view.id ? !view.default : false;
			view.settings = JSON.stringify(view.settings);
		});
		this.viewSvc.updateMultiple(this.views).subscribe((value) => {
			this.setViews(value, this.view.id);
			this.msgSvc.add({
				severity: 'success',
				summary: `All set!`,
				detail: `Default view updated`,
			});
		});
	}

	cancelNewView() {
		this.showNewViewName = false;
		this.newViewName = '';
	}

	deleteView() {
		if (!confirm('Are you sure?')) {
			return;
		}
		this.viewSvc.deleteView(this.view.id).subscribe((value) => {
			this.views = this.views.filter((x) => {
				return x.id !== this.view.id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `bye, bye!`,
				detail: `View was deleted`,
			});
			this.view = null;
			this.updateColumnsSelection.emit(this.columnFilter);
		});
	}
}
