import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { ProcessService } from './process.service';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
	selector: 'app-process',
	template: ` <router-outlet></router-outlet> `,
})
export class ProcessComponent implements OnInit {
	constructor(
		private sidebarSvc: SidebarService,
		private processService: ProcessService,
		private sessionService: SessionService
	) {}

	ngOnInit() {
		this.sidebarSvc.title = 'Process';
		this.processService.myProcesses().subscribe((processes) => {
			this.sessionService.updateMyProcesses(processes);
		});
	}
}
