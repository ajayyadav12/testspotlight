// tslint:disable
import { TestBed } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ProcessDtlNotificationsAlertsComponent } from './process-dtl-notifications-alerts.component';
import { ProcessService } from '../../../process.service';
import { MessageService } from 'primeng/api';
import { SharedModule } from 'src/app/shared/shared.module';
import { Observable, of } from 'rxjs';
import { SessionService } from 'src/app/core/session/session.service';

@Injectable()
class MockProcessService {

  getProcess(processId): Observable<any> {
    return of({
      longRunningSubAlrt: true,
      submissionEscalationAlrt: true,
      longRunningStepAlrt: true,
      submissionDelayedEscalationAlrt: true,
      requiredStepAlrt: true
    })
  }

}

@Injectable()
class MockSessionService {
  role = 'user'
}

describe('ProcessDtlNotificationsAlertsComponent', () => {
  let fixture;
  let component;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SharedModule],
      declarations: [
        ProcessDtlNotificationsAlertsComponent
      ],
      providers: [
        { provide: ProcessService, useClass: MockProcessService },
        { provide: SessionService, useClass: MockSessionService },
        MessageService,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(ProcessDtlNotificationsAlertsComponent);
    component = fixture.debugElement.componentInstance;
  });

  it('should create a component', async () => {
		expect(component).toBeTruthy();
	});

  it('should run #getProcessAlerts()', async () => {
    component.getProcessAlerts();

    expect(component.alertSettings).toEqual({
      longRunningSubAlrt: true,
      submissionEscalationAlrt: true,
      longRunningStepAlrt: true,
      submissionDelayedEscalationAlrt: true,
      requiredStepAlrt: true
    })
  });

});
