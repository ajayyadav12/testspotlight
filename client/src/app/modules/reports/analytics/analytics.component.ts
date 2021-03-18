import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
	selector: 'app-analytics',
	templateUrl: './analytics.component.html',
	styleUrls: ['./analytics.component.scss'],
})
export class AnalyticsComponent implements OnInit {
	analyticsMenu = [
		{ label: 'Trending', routerLink: 'trending' },
		{ label: 'Variance', routerLink: 'variance' },
		{ label: 'Status', routerLink: 'status' },
		{ label: 'Systems Load', routerLink: 'systems-load' },
	];
	activeItem;
	constructor(private sidebarSvc: SidebarService) {
		this.sidebarSvc.title = 'Analytics Report';
	}

	ngOnInit() {
		this.activeItem = this.analyticsMenu[0];
	}
}
