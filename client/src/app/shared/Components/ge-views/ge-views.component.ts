import { MessageService } from 'primeng/api';
import { ViewsService } from './views.service';
import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-ge-views',
  template: `
    <div style="display:inline-block">
      <div class="ui-inputgroup" *ngIf="!showNewViewName">
        <p-dropdown
          [style]="{ 'min-width': '200px' }"
          [options]="views"
          [(ngModel)]="view"
          placeholder="Select a View"
          optionLabel="name"
          [showClear]="true"
          (onChange)="onSelectView($event)"
        ></p-dropdown>
        <button
          pButton
          type="button"
          icon="pi pi-save"
          class="ui-button-secondary"
          (click)="showNewViewInput()"
        ></button>
        <button
          [disabled]="!view"
          pButton
          type="button"
          icon="pi pi-trash"
          class="ui-button-secondary"
          (click)="deleteView()"
        ></button>
      </div>
      <div class="ui-inputgroup" *ngIf="showNewViewName">
        <input
          pTextInput
          (keyup.enter)="onSaveView()"
          [(ngModel)]="newViewName"
          placeholder="Name your view"
          autofocus
          [autofocus]
        />
        <button
          [disabled]="newViewName === ''"
          pButton
          type="button"
          icon="pi pi-check"
          class="ui-button-success"
          (click)="onSaveView()"
        ></button>
        <button pButton type="button" icon="pi pi-times" class="ui-button-secondary" (click)="cancelNewView()"></button>
      </div>
    </div>
  `,
  providers: [ViewsService]
})
export class GEViewsComponent implements OnInit {
  @Input() tParams;
  @Input() moduleName: string;
  @Input() navigate: string;

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

  /**
   * Get user views, select latest view and update queries
   */
  getViews() {
    this.viewSvc.getViews(this.moduleName).subscribe(value => {
      this.views = value;
      this.view = JSON.parse(localStorage.getItem(this.moduleName + '-view'));
      this.onSelectView(this.view);
    });
  }

  /**
   * Update QueryParams and Latest view in local storage
   * @param value Unused
   */
  onSelectView(value) {
    const tValue = this.view ? JSON.parse(this.view.settings) : null;
    if (tValue) {
      localStorage.setItem(this.moduleName + '-chip-filters', JSON.stringify(this.view.chipFilters));
      localStorage.setItem(this.moduleName + '-view', JSON.stringify(this.view));
    } else {
      localStorage.removeItem(this.moduleName + '-view');
      localStorage.removeItem(this.moduleName + '-chip-filters');
    }
    if (this.navigate) {
      this.router.navigate([this.navigate], { queryParams: tValue });
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
        chipFilters: chipFilters
      })
      .subscribe(value => {
        this.showNewViewName = false;
        this.newViewName = '';
        this.views.push(value);
        this.view = value;
        this.msgSvc.add({
          severity: 'success',
          summary: `All set!`,
          detail: `New custom view added`
        });
        localStorage.setItem(this.moduleName + '-view', JSON.stringify(value));
      });
  }

  cancelNewView() {
    this.showNewViewName = false;
    this.newViewName = '';
  }

  deleteView() {
    if (!confirm('Are you sure?')) return;
    this.viewSvc.deleteView(this.view.id).subscribe(value => {
      this.views = this.views.filter(x => {
        return x.id !== this.view.id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `bye, bye!`,
        detail: `View was deleted`
      });
      this.view = null;
    });
  }
}
