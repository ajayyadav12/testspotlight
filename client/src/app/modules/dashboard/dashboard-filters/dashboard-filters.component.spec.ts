// tslint:disable
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { MessageService } from 'primeng/api';
import { Component, Directive } from '@angular/core';
import { DashboardFiltersComponent } from './dashboard-filters.component';
import { Router, ActivatedRoute } from '@angular/router';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { FormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GEChipFilter } from './GEChipFilter';

describe('DashboardFiltersComponent', () => {
  let fixture;
  let component: DashboardFiltersComponent;

  let router: Router;

  const fakeActivatedRoute = {
    snapshot: {
      queryParams: {
        childId: '1,2,'
      },
      paramMap: {
        get(param) {
          return '1';
        }
      }
    },
    queryParams: {
      subscribe(params) {
        return {};
      },
      returnUrl: '/'
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        PrimengModule,
        FormsModule,
        SharedModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([]),
        BrowserAnimationsModule
      ],
      declarations: [DashboardFiltersComponent],
      providers: [MessageService, { provide: ActivatedRoute, useFactory: () => fakeActivatedRoute }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(DashboardFiltersComponent);
    component = fixture.debugElement.componentInstance;
    router = TestBed.get(Router);
  });

  it('should create a component', async () => {
    expect(component).toBeTruthy();
  });

  it('should run #onChangeParams()', async () => {
    component.view = {};
    const chipParams: GEChipFilter[] = [{ id: 1, name: 'test', paramName: 'childId' }];

    component.onChangeParams(chipParams);

    expect(component.chipFilters).toEqual(chipParams);
    expect(component.view).toBeNull();
  });

  it('should run #onRemoveFilter()', async () => {
    const spy = spyOn(router, 'navigate');

    component.onRemoveFilter({
      value: {
        paramName: 'childId',
        id: 1
      }
    });

    expect(spy).toHaveBeenCalledWith(['/dashboard'], {
      queryParams: {
        childId: '2,'
      },
      queryParamsHandling: 'merge'
    });
  });
});
