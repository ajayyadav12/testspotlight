// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { DashboardComponent } from './dashboard.component';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SubmissionsService } from '../reports/submissions/submissions.service';
import { HttpClientModule } from '@angular/common/http';
import { PrimengModule } from 'src/app/shared/primeng.module';

@Injectable()
class MockSubmissionsService {}

describe('DashboardComponent', () => {
  let fixture;
  let component: DashboardComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, PrimengModule],
      declarations: [DashboardComponent],
      providers: [SidebarService, { provide: SubmissionsService, useClass: MockSubmissionsService }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.debugElement.componentInstance;
  });

  it('should create a component', async () => {
    expect(component).toBeTruthy();
  });

  it('should run #expandModule()', async () => {
    component.expandModule('trending');

    expect(component.displayExpandModule).toBeTruthy();
    expect(component.expandedModule).toBe('trending');
  });
});
