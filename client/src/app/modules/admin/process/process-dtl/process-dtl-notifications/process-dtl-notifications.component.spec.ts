// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { ProcessDtlNotificationsComponent } from './process-dtl-notifications.component';
import { FormBuilder, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../../user/user.service';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable, of } from 'rxjs';

@Injectable()
class MockProcessService {
	updateProcessAlerts(processId, alertSettings): Observable<any> {
		return of({});
	}

	getProcessNotifications(processId): Observable<any> {
		return of([{ id: 1 }]);
	}

	getProcess(processId): Observable<any> {
		return of({
			longRunningSubAlrt: true,
			submissionEscalationAlrt: true,
			longRunningStepAlrt: true,
			submissionDelayedEscalationAlrt: true,
			requiredStepAlrt: true,
		});
	}

	newProcessNotification(processId, payload): Observable<any> {
		return of({});
	}

	updateProcessNotification(processId, notificationId, payload): Observable<any> {
		return of({});
	}

	newProcessMyNotification(proceesId, myNotification): Observable<any> {
		return of({});
	}

	updateProcessMyNotification(processId, notificationId, myNotification): Observable<any> {
		return of({});
	}
}

@Injectable()
class MockUserService {}

describe('ProcessDtlNotificationsComponent', () => {
	let fixture;
	let component: ProcessDtlNotificationsComponent;

	const fakeActivatedRoute = {
		snapshot: {
			queryParams: {
				returnUrl: '/',
			},
			paramMap: {
				get(param) {
					return '1';
				},
			},
		},
		queryParams: {
			subscribe(params) {
				return {};
			},
			returnUrl: '/',
		},
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [
				PrimengModule,
				SharedModule,
				HttpClientTestingModule,
				RouterTestingModule,
				ReactiveFormsModule,
				FormsModule,
			],
			declarations: [ProcessDtlNotificationsComponent],
			providers: [
				FormBuilder,
				{ provide: ProcessService, useClass: MockProcessService },
				MessageService,
				{ provide: ActivatedRoute, useFactory: () => fakeActivatedRoute },
				{ provide: UserService, useClass: MockUserService },
			],
			schemas: [CUSTOM_ELEMENTS_SCHEMA],
		}).compileComponents();
		fixture = TestBed.createComponent(ProcessDtlNotificationsComponent);
		component = fixture.debugElement.componentInstance;
	});

	it('should create a component', async () => {
		expect(component).toBeTruthy();
	});

	it('should run #onSubmit() when notificationId === 0', async () => {
		component.notificationId = 0;
		component.getProcessNotifications = jest.fn();
		component.setupForm = jest.fn();
		component.isFormValid = jest.fn().mockReturnValue(true);
		component.getNotificationPayload = jest
			.fn()
			.mockReturnValue({ processStep: null, status: 1, enableTextMessaging: false, escalationType: false });
		component.processNotificationForm.patchValue({ status: 1 });
		component.onSubmit();

		expect(component.notificationId).toBe(-1);
		expect(component.setupForm).toHaveBeenCalled();
		expect(component.getNotificationPayload).toHaveBeenCalled();
		expect(component.getProcessNotifications).toHaveBeenCalled();
	});

	it('should run #onSubmit() when notificationId !== 0', async () => {
		component.notificationId = 1;
		component.getProcessNotifications = jest.fn();
		component.setupForm = jest.fn();
		component.isFormValid = jest.fn().mockReturnValue(true);
		component.getNotificationPayload = jest
			.fn()
			.mockReturnValue({ processStep: null, status: 1, enableTextMessaging: false, escalationType: false });
		component.processNotificationForm.patchValue({ status: 1 });
		component.onSubmit();

		expect(component.notificationId).toBe(-1);
		expect(component.setupForm).toHaveBeenCalled();
		expect(component.getNotificationPayload).toHaveBeenCalled();
		expect(component.getProcessNotifications).toHaveBeenCalled();
	});

	it('should run #getProcessNotifications()', async () => {
		component.notificationMapping = jest.fn((x) => {});

		component.getProcessNotifications();

		expect(component.processNotifications.length).toBeGreaterThan(0);
		expect(component.notificationMapping).toHaveBeenCalledTimes(component.processNotifications.length);
	});

	it('should run #cancel()', async () => {
		component.notificationId = 1;

		component.cancel();

		expect(component.notificationId).toBe(-1);
	});

	it('should run #newNotification()', async () => {
		component.processNotificationForm.reset = jest.fn();
		component.onChangeLevel = jest.fn();
		component.newNotification();

		expect(component.notificationId).toBe(0);
		expect(component.processNotificationForm.reset).toHaveBeenCalled();
	});
});
