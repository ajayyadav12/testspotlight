// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { ProcessDtlSummaryComponent } from './process-dtl-summary.component';
import { ProcessService } from '../../process.service';
import { SystemService } from 'src/app/modules/admin/system/system.service';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SessionService } from 'src/app/core/session/session.service';
import { UserService } from './../../../user/user.service';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

@Injectable()
class MockSessionService {
  get role() {
    return 'admin';
  }
}

@Injectable()
class MockProcessService {
  getToken(id) {
    return of({ token: 'skk' });
  }
  getAllProcessTypes() {
    return of([]);
  }
  getAllProcesses() {
    return of([]);
  }
  getFeedTypes() {
    return of([]);
  }

  getProcess(id) {
    switch (id) {
      case 1:
        return of({
          name: 'test',
          approved: '1'
        });
      case 2:
        return of({
          name: 'test',
          approved: '1',
          processParent: {
            id: 1
          }
        });
      case 3:
        return of({
          name: 'test',
          approved: 'N'
        });
        break;
    }
  }
}

@Injectable()
class MockProcessTypeService { }


@Injectable()
class MockSystemService {
  getAllSystems() {
    return of([]);
  }
}

@Injectable()
class MockUserService {
  getUsers() {
    return of([]);
  }
}

@Injectable()
class MockRouter {
  navigate = jest.fn();
}

describe('ProcessDtlSummaryComponent', () => {
  let fixture;
  let component: ProcessDtlSummaryComponent;

  const fakeActivatedRoute = {
    parent: {
      params: of({ id: 1 })
    }
  };

  let fakeSidevarSvc = {
    title: 'Spotlight'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PrimengModule, ReactiveFormsModule, RouterTestingModule, HttpClientTestingModule],
      declarations: [ProcessDtlSummaryComponent],
      providers: [
        { provide: ProcessService, useClass: MockProcessService },
        { provide: SystemService, useClass: MockSystemService },
        FormBuilder,
        MessageService,
        { provide: ActivatedRoute, useFactory: () => fakeActivatedRoute },
        { provide: SidebarService, useFactory: () => fakeSidevarSvc },
        { provide: SessionService, useClass: MockSessionService },
        { provide: UserService, useClass: MockUserService },
        { provide: Router, useClass: MockRouter }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(ProcessDtlSummaryComponent);
    component = fixture.debugElement.componentInstance;
  });

  it('should create a component', async () => {
    expect(component).toBeTruthy();
  });

  it('should run #setupForm()', async () => {
    component.setupForm();
    expect(component.processForm).toBeDefined();
  });

  it('should run #processApprovalStatusAction() when approved', async () => {
    component.processApprovalStatusAction('1');
    expect(component.processForm.disabled).toBeFalsy();
  });

  it('should run #processApprovalStatusAction() when non-approved', async () => {
    component.processApprovalStatusAction('N');
    expect(component.processForm.disabled).toBeTruthy();
  });

  it('should run #getProcessToken()', async () => {
    component.getProcessToken();

    expect(component.processToken).toBe('skk');
  });

  it('should run #getProcessToken() for non-approved processes', async () => {
    component.processForm.disable();

    component.getProcessToken();

    expect(component.processToken).not.toBe('skk');
  });
});
