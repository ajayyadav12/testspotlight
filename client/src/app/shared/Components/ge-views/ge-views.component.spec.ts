// tslint:disable
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { GEViewsComponent } from './ge-views.component';
import { ViewsService } from './views.service';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PrimengModule } from '../../primeng.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, Observable } from 'rxjs';
@Injectable()
class MockViewsService {
  getViews(moduleName: string): Observable<any> {
    return of([{ name: 'test', id: 1 }]);
  }

  saveView(view): Observable<any> {
    return of({ name: 'test', id: 1 });
  }

  deleteView(id): Observable<any> {
    return of({ name: 'test', id: 1 });
  }
}

@Injectable()
class MockRouter {
  navigate = jest.fn();
}

describe('GEViewsComponent', () => {
  let fixture;
  let component: GEViewsComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PrimengModule, FormsModule, HttpClientTestingModule],
      declarations: [GEViewsComponent],
      providers: [
        { provide: ViewsService, useClass: MockViewsService },
        MessageService,
        { provide: Router, useClass: MockRouter }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    TestBed.overrideComponent(GEViewsComponent, {
      set: {
        providers: [{ provide: ViewsService, useClass: MockViewsService }]
      }
    });

    fixture = TestBed.createComponent(GEViewsComponent);
    component = fixture.debugElement.componentInstance;
  });

  it('should create a component', async () => {
    expect(component).toBeTruthy();
  });

  it('should run #ngOnInit()', async () => {
    component.getViews = jest.fn();
    component.ngOnInit();

    expect(component.getViews).toHaveBeenCalled();
  });

  it('should run #getViews()', async () => {
    component.onSelectView = jest.fn(x => true);

    component.getViews();
    expect(component.view).toBeNull();
    expect(component.views.length).toBeGreaterThan(0);
    expect(component.onSelectView).toHaveBeenCalled();
  });

  it('should run #showNewViewInput()', async () => {
    component.showNewViewName = false;
    component.newViewName = 'Test';

    component.showNewViewInput();

    expect(component.showNewViewName).toBeTruthy();
    expect(component.newViewName).toBe('');
  });

  it('should run #onSaveView()', async () => {
    component.onSaveView();

    expect(component.showNewViewName).toBeFalsy();
    expect(component.newViewName).toBe('');
    expect(component.views.length).toBeGreaterThan(0);
    expect(component.view).toBeDefined();
  });

  it('should run #cancelNewView() and reset view settings', async () => {
    component.showNewViewName = true;
    component.newViewName = 'Test';

    component.cancelNewView();

    expect(component.showNewViewName).toBeFalsy();
    expect(component.newViewName).toBe('');
  });

  it('should run #deleteView() and delete from views', async () => {
    component.views = [{ name: 'test', id: 1 }];
    component.view = { name: 'test', id: 1 };
    window.confirm = jest.fn(x => true);

    component.deleteView();

    expect(component.views.length).toBe(0);
  });

  it('should run #deleteView() and NOT delete from views', async () => {
    component.views = [{ name: 'test', id: 1 }];
    component.view = { name: 'test', id: 1 };
    window.confirm = jest.fn(x => false);

    component.deleteView();

    expect(component.views.length).toBe(1);
  });
});
