// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ProcessService } from '../../process/process.service';
import { ScheduleService } from '../schedule.service';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { ScheduleDtlFormComponent } from './schedule-dtl-form/schedule-dtl-form.component';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable()
class MockSidebarService {
	title = '';
}

@Injectable()
class MockEnvConfigurationService {
	appConfig = {
		environment: 'local',
		apiURL: 'http://localhost:4200/dashapi/v1',
		appsApiUrl: 'http://localhost:9001/appsapi/v1/submissions/steps/',
		oidcOauthUri: 'https://fssfed.ge.com/fss/as/authorization.oauth2',
		oidcClientId: 'GECORP_Spotlight_Dev_Client',
		logoutUrl: 'https://ssologin.ssogen2.corporate.ge.com/logoff/logoff.jsp',
		version: '-dev',
		instrumentationKey: '',
	};
}

@Injectable()
class MockProcessService {}

@Injectable()
class MockScheduleService {}

@Injectable()
class MockRouter {
	navigate = jest.fn();
}

describe('ScheduleDtlFormComponent', () => {
	let fixture;
	let component: ScheduleDtlFormComponent;

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
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [
				SharedModule,
				PrimengModule,
				FormsModule,
				ReactiveFormsModule,
				HttpClientTestingModule,
				RouterTestingModule,
			],
			declarations: [ScheduleDtlFormComponent],
			providers: [
				FormBuilder,
				{ provide: ProcessService, useClass: MockProcessService },
				{ provide: ScheduleService, useClass: MockScheduleService },
				{ provide: Router, useClass: MockRouter },
				{ provide: ActivatedRoute, useFactory: () => fakeActivatedRoute },
				MessageService,
				{ provide: SidebarService, useClass: MockSidebarService },
				AuditLogService,
				{ provide: EnvConfigurationService, useClass: MockEnvConfigurationService },
			],
			schemas: [CUSTOM_ELEMENTS_SCHEMA],
		}).compileComponents();
		fixture = TestBed.createComponent(ScheduleDtlFormComponent);
		component = fixture.debugElement.componentInstance;
	});

	it('should create a component', async () => {
		expect(component).toBeTruthy();
	});

	/* it('should run #ngOnInit()', async () => {
     component.disableEnableMonthControls = jest.fn(x => { })
     component.disableEnableDailyControls = jest.fn(x => { })
 
     component.ngOnInit();
 
     expect(component.disableEnableMonthControls).toHaveBeenCalledWith('day');
     expect(component.disableEnableDailyControls).toHaveBeenCalledWith('daily');
   });*/

	it('should run #disableEnableMonthControls()', async () => {
		// const result = component.disableEnableMonthControls(option);
	});

	it('should run #disableEnableDailyControls()', async () => {
		// const result = component.disableEnableDailyControls(option);
	});

	it('should run #getSchedule()', async () => {
		// const result = component.getSchedule();
	});

	it('should run #getSchedulePayload()', async () => {
		// const result = component.getSchedulePayload();
	});

	it('should run #minLengthArray()', async () => {
		// const result = component.minLengthArray(min);
	});

	it('should run #checkRequired()', async () => {
		// const result = component.checkRequired();
	});

	it('should run #noDuplicatesArray()', async () => {
		// const result = component.noDuplicatesArray();
	});

	it('should run #onClickRecurrencePattern()', async () => {
		// const result = component.onClickRecurrencePattern();
	});

	it('should run #onDailyOptionChanged()', async () => {
		// const result = component.onDailyOptionChanged();
	});

	it('should run #onMonthOptionChanged()', async () => {
		// const result = component.onMonthOptionChanged();
	});

	it('should run #onChangeDuration()', async () => {
		const now = new Date();
		const duration = 60;
		component.scheduleForm.patchValue({
			startTime: now,
		});

		component.onChangeDuration({ value: duration });

		expect(component.scheduleForm.get('endTime').value).toEqual(new Date(now.getTime() + duration * 60000));
	});

	it('should run #onChangeEndDate()', async () => {
		// const result = component.onChangeEndDate(event);
	});

	it('should run #onOneYearAheadEndDate()', async () => {
		// const result = component.onOneYearAheadEndDate(event);
	});

	it('should run #save() New', async () => {
		component.incomingScheduleId = 0;
		component.newSchedule = jest.fn((x) => {});

		component.save();

		expect(component.newSchedule).toHaveBeenCalled();
	});

	it('should run #save() Update', async () => {
		component.incomingScheduleId = 1;
		component.updateSchedule = jest.fn((x) => {});

		component.save();

		expect(component.updateSchedule).toHaveBeenCalled();
	});

	it('should run #newSchedule()', async () => {
		// const result = component.newSchedule(requestPayload);
	});

	it('should run #updateSchedule()', async () => {
		// const result = component.updateSchedule(requestPayload);
	});

	it('should run #setupForm()', async () => {
		// const result = component.setupForm();
	});

	it('should run #setupFormValidation()', async () => {
		// const result = component.setupFormValidation();
	});

	it('should run #afterChangingStartTime()', async () => {
		component.duration = 15;
		const startTime = new Date();
		const newTime = new Date(startTime.getTime() + component.duration * 60000);

		component.afterChangingStartTime(startTime);

		expect(component.scheduleForm.get('endTime').value).toEqual(newTime);
	});

	it('should run #afterChangingEndTime()', async () => {
		const newDuration = 15;
		const startTime = new Date();
		const endTime = new Date(startTime.getTime() + newDuration * 60000);

		component.afterChangingEndTime(startTime, endTime);

		expect(component.duration).toBe(newDuration);
	});

	it('should run #setupCombos()', async () => {
		// const result = component.setupCombos();
	});
});
