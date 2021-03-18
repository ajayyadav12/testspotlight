import { Component, OnInit, ViewEncapsulation, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { GEChipFilter } from './GEChipFilter';
import { GEFiltersComponent } from 'src/app/shared/Components/ge-filters/ge-filters.component';

@Component({
	selector: 'app-dashboard-filters',
	templateUrl: './dashboard-filters.component.html',
	styleUrls: ['./dashboard-filters.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class DashboardFiltersComponent implements OnInit {
	@ViewChild('filter', { static: true })
	filter: GEFiltersComponent;
	filterSelection = ['process', 'parent', 'days'];

	params;

	view;
	autoRefreshOn = true;
	autoRefreshHandler;
	timing: number = localStorage.getItem('dashboard-autorefresh-timing')
		? Number.parseInt(localStorage.getItem('dashboard-autorefresh-timing'))
		: 20;
	constructor(private router: Router, private activatedRoute: ActivatedRoute) {}

	ngOnInit() {
		this.onchangeTiming(null);
		this.activatedRoute.queryParams.subscribe((params) => {
			this.params = params;
			this.filter.updateFilters();
		});
	}

	onClickRefresh() {
		this.router.navigate(['/dashboard'], {
			queryParams: { refresh: Math.random() * 10 },
			queryParamsHandling: 'merge',
		});
	}

	onchangeTiming(timing) {
		clearInterval(this.autoRefreshHandler);
		localStorage.setItem('dashboard-autorefresh-timing', this.timing.toString());
		if (this.timing > 0) {
			this.autoRefreshOn = true;
			this.autoRefreshSetup();
		} else {
			this.autoRefreshOn = false;
		}
	}

	ngOnDestroy() {
		clearInterval(this.autoRefreshHandler);
	}

	autoRefreshSetup() {
		this.autoRefreshHandler = setInterval(() => {
			this.onClickRefresh();
		}, 60000 * this.timing);
	}
}
