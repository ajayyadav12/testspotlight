import { Component, OnInit, ViewEncapsulation, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { GEChipFilter } from './GEChipFilter';
import { GEFiltersComponent } from 'src/app/shared/Components/ge-filters/ge-filters.component';
import { RelationshipService } from 'src/app/modules/relationship/relationship.service';

const DAYS_DEFAULT = 7;

@Component({
	selector: 'app-ops-dashboard-filters',
	templateUrl: './dashboard-ops-filters.component.html',
	styleUrls: ['./dashboard-ops-filters.component.scss'],
	encapsulation: ViewEncapsulation.None,
	providers: [RelationshipService]
})
export class DashboardOpsFiltersComponent implements OnInit {
	@ViewChild('filter', { static: true })
	hide = false;
	filterSelection = ['area', 'finance', 'process'];
	params;

	area = [{ label: 'Data Management', value: 6 }, { label: 'Finance', value: 0 }, { label: 'Reporting', value: 5 }];
	finance = [{ label: 'Subledger', value: 1 }, { label: 'Business Consolidation', value: 3 }, { label: 'ERP/GL', value: 2 }, { label: 'TC Consolidation', value: 4 }];

	systems = [];
	selectedSystem;
	selectedClosePhaseid = 0;
	selectedArea = 0;

	view;
	autoRefreshOn = true;
	autoRefreshHandler;
	timing: number = localStorage.getItem('dashboard-autorefresh-timing')
		? Number.parseInt(localStorage.getItem('dashboard-autorefresh-timing'))
		: 20;
	constructor(private relationshipSvc: RelationshipService, private router: Router, private activatedRoute: ActivatedRoute) {

	}

	ngOnInit() {
		this.activatedRoute.queryParams.subscribe(params => {
			this.params = params;
//			this.filter.updateFilters();
		});

	}



	showFilter(filterName: string): boolean {
		return this.filterSelection.includes(filterName);
	}

	openSystemSelector() {		

		this.relationshipSvc.getSystemsByClosePhase(this.selectedClosePhaseid).subscribe((value) => {
			this.systems = value;
		});
	}

	reset() {

	}

	resetFilters() {
    this.selectedArea = 0;
    this.selectedClosePhaseid = 0;
    this.selectedSystem = 0;
    this.filter();
    this.openSystemSelector();
    this.openClosePhaseRelationship();
  }

	filter(){
		this.relationshipSvc.getSystemsByClosePhase(this.selectedArea).subscribe((value) => {
			this.systems = value;
		});
	}

	openClosePhaseRelationship() {	
		let queryParams; 
		if (this.selectedSystem) {
			queryParams = {			
				systemId: this.selectedSystem ? this.selectedSystem.id : '-1'
			};
			this.router.navigate(['/dashboard'],{ queryParams: queryParams, queryParamsHandling: 'merge' });		
		} else {
			this.router.navigate(['/dashboard']);		
		} 
		
	}
}
