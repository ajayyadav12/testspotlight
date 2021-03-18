import { Component, OnInit, HostListener, ViewChild, AfterViewInit } from '@angular/core';
import { MessageService, MenuItem } from 'primeng/api';
import { SidebarService } from '../core/sidebar/sidebar.service';
import { trigger, transition, style, animate } from '@angular/animations';
import { SessionService } from '../core/session/session.service';
import { OverlayPanel } from 'primeng/overlaypanel';
import { GuidedTourService } from '../core/services/guided-tour.service';
import { EnvConfigurationService } from '../core/services/env-configuration.service';

@Component({
	selector: 'app-sidebar',
	templateUrl: './sidebar.component.html',
	styleUrls: ['./sidebar.component.scss'],
	animations: [
		trigger('sidebarAnimation', [
			transition(':enter', [style({ left: '-250px' }), animate('.2s', style({ left: '0px' }))]),
			transition(':leave', [animate('.2s', style({ left: '-250px' }))]),
		]),
	],
})
export class SidebarComponent implements OnInit, AfterViewInit {
	@ViewChild(OverlayPanel) gda1: OverlayPanel;
	version;
	buildDate;
	lastSize = 0;

	get isLoggedIn(): boolean {
		if (this.sessionSvc.token) {
			return true;
		} else {
			return false;
		}
	}

	constructor(
		public sidebarSvc: SidebarService,
		private sessionSvc: SessionService,
		private msgSvc: MessageService,
		private guiderTourSvc: GuidedTourService,
		private envSvc: EnvConfigurationService
	) {}

	ngAfterViewInit(): void {
		this.guiderTourSvc.a1 = this.gda1;
	}

	ngOnInit() {
		this.lastSize = window.innerWidth;
		this.version = this.envSvc.appConfig.version;
	}

	@HostListener('window:resize', ['$event'])
	onResize(event) {
		if (window.innerWidth <= 850 && this.lastSize > 850) {
			this.sidebarSvc.sidebarOpen = false;
		}
		this.lastSize = window.innerWidth;
	}
}
