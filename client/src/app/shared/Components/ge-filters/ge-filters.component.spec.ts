// tslint:disable
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { GEFiltersComponent } from './ge-filters.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { GEFiltersService } from './ge-filters.service';
import { Router, ActivatedRoute } from '@angular/router';
import { PrimengModule } from '../../primeng.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, Observable } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

@Injectable()
class MockGEFiltersService {
  getAllSenders(): Observable<any> {
    return of([
      {
        id: 1,
        name: 'sender'
      }
    ]);
  }

  getAllReceiver(): Observable<any> {
    return of([
      {
        id: 1,
        name: 'receiver'
      }
    ]);
  }

  getAllProcesses(x: boolean): Observable<any> {
    return of([
      {
        id: 1,
        isParent: true
      },
      {
        id: 1,
        isParent: false
      }
    ]);
  }
}

describe('GEFiltersComponent', () => {
  let fixture;
  let component: GEFiltersComponent;

  let router: Router;

  beforeEach(() => {
    const fakeActivatedRoute = {
      snapshot: {
        queryParams: {
          childId: '1,2,3,',
          status: '1,'
        },
        paramMap: {
          get(param) {
            return '1';
          }
        }
      }
    };

    TestBed.configureTestingModule({
      imports: [
        PrimengModule,
        ReactiveFormsModule,
        HttpClientTestingModule,
        BrowserAnimationsModule,
        RouterTestingModule.withRoutes([])
      ],
      declarations: [GEFiltersComponent],
      providers: [
        FormBuilder,
        { provide: GEFiltersService, useClass: MockGEFiltersService },
        { provide: ActivatedRoute, useFactory: () => fakeActivatedRoute }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    TestBed.overrideComponent(GEFiltersComponent, {
      set: {
        providers: [{ provide: GEFiltersService, useClass: MockGEFiltersService }]
      }
    });
    fixture = TestBed.createComponent(GEFiltersComponent);
    component = fixture.debugElement.componentInstance;
    router = TestBed.get(Router);
  });

  it('should create a component', async () => {
    expect(component).toBeTruthy();
  });

  it('should run #filter() for PR', async () => {
    component.submissionFilterForm.patchValue({
      level: 'PR',
      processes: [],
      status: []
    });
    const navigateSpy = spyOn(router, 'navigate');

    component.filter();

    expect(navigateSpy).toHaveBeenCalled();
  });

  it('should run #filter() for PA', async () => {
    component.submissionFilterForm.patchValue({
      level: 'PA',
      processes: [],
      status: []
    });
    const navigateSpy = spyOn(router, 'navigate');

    component.filter();

    expect(navigateSpy).toHaveBeenCalled();
  });

  it('should run #filter() for SR', async () => {
    component.submissionFilterForm.patchValue({
      level: 'SR',
      processes: [],
      status: []
    });
    const navigateSpy = spyOn(router, 'navigate');

    component.filter();

    expect(navigateSpy).toHaveBeenCalled();
  });

  it('should run #reset()', async () => {
    component.filterLevelRules = jest.fn();
    component.filtersSetup = jest.fn();

    component.reset();

    expect(component.submissionFilterForm.value).toEqual({
      from: null,
      to: null,
      level: 'PR',
      processes: { value: [] },
      status: { value: [] },
      bu: null,
      altId: null,
      adHoc: null
    });
    expect(component.filterLevelRules).toHaveBeenCalled();
    expect(component.filtersSetup).toHaveBeenCalled();
  });

  it('should run #filterLevelRules() when level is "PR"', async () => {
    component.submissionFilterForm.patchValue({ level: 'PR' });

    component.filterLevelRules();

    expect(component.submissionFilterForm.get('processes').enabled).toBeTruthy();
    expect(component.submissionFilterForm.get('parents').disabled).toBeTruthy();
    expect(component.submissionFilterForm.get('receivers').disabled).toBeTruthy();
    expect(component.submissionFilterForm.get('senders').disabled).toBeTruthy();
  });

  it('should run #filterLevelRules() when level is "PA"', async () => {
    component.submissionFilterForm.patchValue({ level: 'PA' });

    component.filterLevelRules();

    expect(component.submissionFilterForm.get('processes').disabled).toBeTruthy();
    expect(component.submissionFilterForm.get('parents').enabled).toBeTruthy();
    expect(component.submissionFilterForm.get('receivers').disabled).toBeTruthy();
    expect(component.submissionFilterForm.get('senders').disabled).toBeTruthy();
  });

  it('should run #filterLevelRules() when level is "SR"', async () => {
    component.submissionFilterForm.patchValue({ level: 'SR' });

    component.filtersSetup();

    expect(component.submissionFilterForm.get('processes').disabled).toBeTruthy();
    expect(component.submissionFilterForm.get('parents').disabled).toBeTruthy();
    expect(component.submissionFilterForm.get('receivers').enabled).toBeTruthy();
    expect(component.submissionFilterForm.get('senders').enabled).toBeTruthy();
  });

  it('should run #filtersSetup()', async () => {
    component.filtersSetup();

    expect(component.senders.length).toBeGreaterThan(0);
    expect(component.receivers.length).toBeGreaterThan(0);
    expect(component.processes.length).toBeGreaterThan(0);
    expect(component.parentProcesses.length).toBeGreaterThan(0);
  });

  it('should run #showFilter()', async () => {
    const result = component.showFilter('processes');
    expect(result).toBeTruthy();
  });

  it('should update form according to queryParams when opening sidebar', async () => {
    component.processes = [{ id: 1, name: 'test1' }, { id: 2, name: 'test2' }, { id: 3, name: 'test3' }];
    component.onShowSidebar(true);
    expect(component.submissionFilterForm.value).toEqual({
      adHoc: null,
      altId: null,
      bu: null,
      from: null,
      level: 'PR',
      processes: [
        {
          id: 1,
          name: 'test1'
        },
        {
          id: 2,
          name: 'test2'
        },
        {
          id: 3,
          name: 'test3'
        }
      ],
      status: [
        {
          label: 'In Progress',
          value: 1
        }
      ],
      to: null
    });
  });
});
