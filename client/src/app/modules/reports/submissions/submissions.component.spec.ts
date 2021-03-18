// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { SubmissionsComponent } from './submissions.component';
import { SubmissionsService } from './submissions.service';
import { ActivatedRoute } from '@angular/router';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { MessageService } from 'primeng/api';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SharedModule } from 'src/app/shared/shared.module';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { Observable, of } from 'rxjs';
import * as moment from 'moment-timezone';
import { EnvConfigurationService } from 'src/app/core/services/env-configuration.service';

@Injectable()
class MockSubmissionsService {
	getSubmissions(queryParams): Observable<any> {
		return of({ totalElements: 1, content: [{ id: 1 }] });
	}
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

describe('SubmissionsComponent', () => {
	let fixture;
	let component: SubmissionsComponent;

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
				HttpClientTestingModule,
				SharedModule,
				PrimengModule,
				RouterTestingModule,
				CommonModule,
				FormsModule,
				BrowserAnimationsModule,
			],
			declarations: [SubmissionsComponent],
			providers: [
				{ provide: SubmissionsService, useClass: MockSubmissionsService },
				{ provide: ActivatedRoute, useFactory: () => fakeActivatedRoute },
				SidebarService,
				MessageService,
				AuditLogService,
				{ provide: EnvConfigurationService, useClass: MockEnvConfigurationService },
			],
			schemas: [CUSTOM_ELEMENTS_SCHEMA],
		}).compileComponents();
		fixture = TestBed.createComponent(SubmissionsComponent);
		component = fixture.debugElement.componentInstance;
	});

	it('should create a component', async () => {
		expect(component).toBeTruthy();
	});

	it('should run #ngOnInit()', async () => {
		component.ngOnInit();

		expect(component.displayType).toBe(1);
	});

	it('should run #autoRefreshSetup()', async () => {
		component.autoRefreshSetup();

		expect(component.autoRefreshHandler).toBeDefined();
	});

	it('should run #onchangeTiming() - Timng > 0', async () => {
		component.timing = 3;
		window.clearInterval = jest.fn((x: any) => {});
		component.autoRefreshSetup = jest.fn(() => {});

		component.onchangeTiming(true);

		expect(window.clearInterval).toHaveBeenCalled();
		expect(component.autoRefreshOn).toBeTruthy();
		expect(component.autoRefreshSetup).toHaveBeenCalled();
	});

	it('should run #onchangeTiming() - Timng <= 0', async () => {
		component.timing = 0;
		window.clearInterval = jest.fn((x: any) => {});

		component.onchangeTiming(true);

		expect(window.clearInterval).toHaveBeenCalled();
		expect(component.autoRefreshOn).toBeFalsy();
	});

	it('should run #ngOnDestroy()', async () => {
		component.showIsolation = true;
		window.clearInterval = jest.fn((x: any) => {});

		component.ngOnDestroy();

		expect(window.clearInterval).toHaveBeenCalled();
		expect(component.showIsolation).toBeFalsy();
	});

	// it('should run #updateTitle()', async () => {
	//   // const result = component.updateTitle(event);
	// });

	// it('should run #getSingleSubmission()', async () => {
	//   // const result = component.getSingleSubmission(index);
	// });

	// it('should run #getLatestStepName()', async () => {
	//   // const result = component.getLatestStepName(steps);
	// });

	it('should run #getSubmissions()', async () => {
		component.totalRecords = 0;
		component.submissionsMapping = jest.fn((x) => {});
		component.getUpdatedSubmissionRequestParams = jest.fn(() => {});
		component.getSubmissions(false);

		expect(component.submissions.length).toBeGreaterThanOrEqual(0);
		expect(component.totalRecords).toBeGreaterThanOrEqual(0);
		expect(component.submissionsMapping).toHaveBeenCalledTimes(0);
	});

	// it('should run #getUpdatedSubmissionRequestParams()', async () => {
	//   // const result = component.getUpdatedSubmissionRequestParams();
	// });

	// it('should run #onSubmissionClose()', async () => {
	//   // const result = component.onSubmissionClose(submission);
	// });

	// it('should run #openSubmissionDialog()', async () => {
	//   // const result = component.openSubmissionDialog(submission);
	// });

	// it('should run #setAcknowledgementFlag()', async () => {
	//   // const result = component.setAcknowledgementFlag(noteValue);
	// });

	it('should run #openNotesDialog()', async () => {
		component.openNotesDialog({ notes: 'notes' });

		expect(component.notesData).toBe('notes');
		expect(component.displayNotesDialog).toBeTruthy();
	});

	it('should run #openIsolationMode()', async () => {
		const s = { id: 1 };
		component.openIsolationMode(s);

		expect(component.showIsolation).toBeTruthy();
		expect(component.isolatedSubmission).toBe(s);
	});

	it('should run #submissionStatusColor()', async () => {
		SubmissionCommon.submissionStatusColor = jest.fn((x) => {
			return '#fff';
		});
		const result = component.submissionStatusColor('warning');

		expect(result).toBe('#fff');
	});

	it('should run #submissionsMapping()', async () => {
		let s = {
			process: {
				name: 'test',
			},
			status: {
				name: 'success',
			},
			startTime: '2019-01-01',
			endTime: '2019-01-02',
		};

		component.submissionsMapping(s);

		expect(s).toEqual({
			color: '#fff',
			elapsedTime: '1d ',
			end: new Date('2019-01-02'),
			endTime: '01/02/19 12:0 am',
			statusValue: 'Success',
			process: {
				name: 'test',
			},
			'process.name': 'test',
			start: new Date('2019-01-01'),
			startTime: '01/01/19 12:0 am',
			status: 'success',
			title: 'test',
			totalTime: '1 days',
			//reportFileUrl: null,
			// dataFileUrl: null
		});
	});

	// it('should run #changeView()', async () => {
	//   // const result = component.changeView(submission);
	// });

	// it('should run #mapParentSubmission()', async () => {
	//   // const result = component.mapParentSubmission(value);
	// });

	it('should run #loadSubmissions() default sortField', async () => {
		component.sortField = null;

		component.loadSubmissions({ sortOrder: null });

		expect(component.sortField).toBe('id');
	});

	it('should run #onClickRefresh()', async () => {
		component.getSubmissions = jest.fn((x) => {});

		component.onClickRefresh();

		expect(component.getSubmissions).toHaveBeenCalledWith(false);
	});

	it('should run #refreshStatus()', async () => {
		component.submissions = [{ id: 1 }, { id: 2 }];
		component.getSingleSubmission = jest.fn((x) => {});
		component.refreshStatus({ id: '2' });

		expect(component.getSingleSubmission).toHaveBeenCalledWith(1);
	});
});
