import { environment } from './../../environments/environment';
import { Component, OnInit, HostListener } from '@angular/core';
import { MessageService, MenuItem } from 'primeng/api';
import { SidebarService } from '../core/sidebar/sidebar.service';
import { trigger, transition, style, animate } from '@angular/animations';
import { SessionService } from '../core/session/session.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  animations: [
    trigger('sidebarAnimation', [
      transition(':enter', [style({ left: '-250px' }), animate('.2s', style({ left: '0px' }))]),
      transition(':leave', [animate('.2s', style({ left: '-250px' }))])
    ])
  ]
})
export class SidebarComponent implements OnInit {
  version = environment.VERSION;
  buildDate;
  lastSize = 0;

  get isLoggedIn(): boolean {
    if (this.sessionSvc.token) {
      return true;
    } else {
      return false;
    }
  }

  get name(): string {
    return this.sessionSvc.name;
  }

  get sso(): string {
    return this.sessionSvc.sso;
  }

  get role(): string {
    return this.sessionSvc.role;
  }

  help: MenuItem[] = [
    {
      label: 'Resources',
      items: [
        {
          label: 'Confluence',
          url: 'https://devcloud.swcoe.ge.com/devspace/display/MIXCE/Spotlight+Home',
          target: '_blank'
        },
        { label: 'Aha', url: 'https://ge-dw.aha.io/products/R2RSPT/feature_cards', target: '_blank' }
      ]
    }
  ];

constructor(public sidebarSvc: SidebarService, private sessionSvc: SessionService, private msgSvc: MessageService) {}

  ngOnInit() {
    this.lastSize = window.innerWidth;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    if (window.innerWidth <= 850 && this.lastSize > 850) {
      this.sidebarSvc.sidebarOpen = false;
    }
    this.lastSize = window.innerWidth;
  }
}
