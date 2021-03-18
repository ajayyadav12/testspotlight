import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

@Component({
	selector: 'app-ge-table',
	templateUrl: './ge-table.component.html',
	styleUrls: ['./ge-table.component.scss']
})
export class GeTableComponent implements OnInit {
	@ViewChild('dt') dt;
	@Input() readonly = false;
	@Input() isLightVersion;
	@Input() customEdit = false;
	@Input() value: any[];
	@Input() columns: any[];
	@Input() routerLink: string;
	@Input() showEditDelete = true;
	@Input() loading = false;
	@Input() menuItems = [];
	@Input() iconColumn = '';
	@Input() columnWithStyle = '';
	@Input() rows = 15;
	@Input() globalFilter = false;
	@Input() showCustomControl = false;
	@Input() iconCustomControl = 'pi-clone';
	@Input() tooltipCustomControl = 'Copy';
	@Output() deleteRecord = new EventEmitter();
	@Output() editRecord = new EventEmitter();
	@Output() customControl = new EventEmitter();
	@Output() clickMenuOption = new EventEmitter();
	@Output() onRowSelect = new EventEmitter();
	@Input() selectedValue: any;
	@Output() changeGlobalFilters = new EventEmitter();

	_menuItems = [];
	recordSelected;

	constructor(private router: Router) { }

	ngOnInit() {
		this.menuItems.forEach(item => {
			this._menuItems.push({
				label: item.label,
				icon: item.icon,
				command: event => {
					this.clickMenuOption.emit({ item: event.item.label, value: this.recordSelected });
				}
			});
		});
		if (localStorage.getItem(this.routerLink + "-edit")) {
			setTimeout(() => {
				this.selectedValue = localStorage.getItem(this.routerLink + "-edit");
				this.dt.filterGlobal(this.selectedValue, 'contains');
			}, 500);
		}
	}

	rowSelect(event) {
		this.onRowSelect.emit(event);
	}

	deleteItem(id) {
		if (this.routerLink === '/process/') {
			if (confirm('All process related data like Submissions,Notifications,Logs will be deleted.Are you sure you want to delete this record?')) {
				this.deleteRecord.emit(id);
			}
		} else {
			if (confirm('Are you sure you want to delete this record?')) {
				this.deleteRecord.emit(id);
			}
		}
	}

	editItem(id) {
		if (this.selectedValue) {
			localStorage.setItem(this.routerLink + '-edit', this.selectedValue)
		}
		if (this.customEdit) {
			this.editRecord.emit(id);
		} else if (this.routerLink) {
			this.router.navigate([this.routerLink, id]);
		} else {
			alert('Edit record is not enabled');
		}
	}

	onCustomControl(id) {
		this.customControl.emit(id);
	}

	onChangeGlobalFilters(event) {
		//this.selectedValue.emit(null);
		this.changeGlobalFilters.emit(event);
	}
}
