// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { DashboardTrendingChartComponent } from './dashboard-trending-chart.component';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable, of } from 'rxjs';
import { CoreModule } from 'src/app/core/core.module';

@Injectable()
class MockSubmissionsService {
	getSubmissionCount(params): Observable<any> {
		return params.days === 30 ? of([ { id: 1 } ]) : of([]);
	}
}

describe('DashboardTrendingChartComponent', () => {
	let fixture;
	let component: DashboardTrendingChartComponent;

	const fakeActivatedRoute = {
		snapshot: {
			queryParams: {
				returnUrl: '/'
			},
			paramMap: {
				get(param) {
					return '1';
				}
			}
		},
		queryParams: {
			subscribe() {
				return of({ days: 14, childId: '1,' });
			},
			returnUrl: '/'
		}
	};

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [ PrimengModule, HttpClientTestingModule, RouterTestingModule, CoreModule ],
			declarations: [ DashboardTrendingChartComponent ],
			providers: [
				{ provide: SubmissionsService, useClass: MockSubmissionsService },
				{ provide: ActivatedRoute, useFactory: () => fakeActivatedRoute }
			],
			schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
		}).compileComponents();
		fixture = TestBed.createComponent(DashboardTrendingChartComponent);
		component = fixture.debugElement.componentInstance;
	});

	it('should create a component', async () => {
		expect(component).toBeTruthy();
	});

	// it('should run #ngAfterViewInit()', async () => {
	//   component.getSubmissionCount = jest.fn(x => {});

	//   component.ngAfterViewInit();

	//   expect(component.days).toBe(14);
	//   expect(component.getSubmissionCount).toHaveBeenCalledWith({ days: 14, childId: '1,' });
	// });

	it('should run #getSubmissionCount() No data found', async () => {
		component.getSubmissionCount();

		expect(component.dataFound).toBeFalsy();
	});

	it('should run #getSubmissionCount() with Data', async () => {
		component.drawChart = jest.fn();

		component.getSubmissionCount({ days: 30 });

		expect(component.dataFound).toBeTruthy();
		expect(component.submissionStats.length).toBeGreaterThan(0);
	});

	it('should run #getUpdatedParams() for Process', async () => {
		const result = component.getUpdatedParams({
			level: 'PR',
			parentId: '1',
			childId: '1',
			receiver: '1',
			sender: '1'
		});
		expect(result.parentId).toBe('-1,');
		expect(result.receiver).toBe('-1,');
		expect(result.sender).toBe('-1,');
	});

	it('should run #getUpdatedParams() for Parent', async () => {
		const result = component.getUpdatedParams({
			level: 'PA',
			parentId: '1',
			childId: '1',
			receiver: '1',
			sender: '1'
		});
		expect(result.childId).toBe('-1,');
		expect(result.receiver).toBe('-1,');
		expect(result.sender).toBe('-1,');
	});

	it('should run #getUpdatedParams() for Sender & Receiver', async () => {
		const result = component.getUpdatedParams({
			level: 'SR',
			parentId: '1',
			childId: '1',
			receiver: '1',
			sender: '1'
		});
		expect(result.childId).toBe('-1,');
		expect(result.parentId).toBe('-1,');
	});

	// it('should run #drawChart()', async () => {
	//   // const result = component.drawChart();
	// });

	// it('should run #drawProcessBarChart()', async () => {
	//   // const result = component.drawProcessBarChart(value);
	// });
});
