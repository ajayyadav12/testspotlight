// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { GeUpdateScheduledSubmissionComponent } from './ge-update-scheduled-submission.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ScheduleSubmissionsService } from 'src/app/modules/admin/schedule/schedule-submissions.service';
import { PrimengModule } from '../../primeng.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Observable, of } from 'rxjs';

describe('GeUpdateScheduledSubmissionComponent', () => {
  @Injectable()
  class MockScheduleSubmissionsService {
    updateUpcomingSubmission(id, start, end): Observable<any> {
      return of({});
    }
  }

  let fixture;
  let component: GeUpdateScheduledSubmissionComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PrimengModule, ReactiveFormsModule, HttpClientTestingModule],
      declarations: [GeUpdateScheduledSubmissionComponent],
      providers: [
        FormBuilder,
        {
          provide: ScheduleSubmissionsService,
          useClass: MockScheduleSubmissionsService
        }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(GeUpdateScheduledSubmissionComponent);
    component = fixture.debugElement.componentInstance;
  });

  it('should create a component', async () => {
    expect(component).toBeTruthy();
  });

  it('should run #updateUpcomingSubmission()', async () => {
    component.onUpdateSubmission.emit = jest.fn(x => { });
    component.setValue({ startTime: new Date(), endTime: new Date(), editNotes: '' });

    component.updateUpcomingSubmission();

    expect(component.onUpdateSubmission.emit).toHaveBeenCalled();
  });

  it('should run #setValue()', async () => {
    component.setValue({ startTime: new Date(), endTime: new Date(), editNotes: '' });

    expect(component.scheduledSubmissionFormValue).toBeDefined();
  });

  it('should run #toggle()', async () => {
    component.toggle();

    expect(component.displayEditDialog).toBeTruthy();
  });
});
