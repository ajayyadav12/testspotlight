import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
	selector: 'app-system',
	template: ` <router-outlet></router-outlet> `,
})
export class SystemComponent implements OnInit {
	constructor(private sidebarSvc: SidebarService) {}

	ngOnInit() {
		this.sidebarSvc.title = 'System';
	}
}
