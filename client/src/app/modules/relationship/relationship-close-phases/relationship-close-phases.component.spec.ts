// tslint:disable
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Injectable, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { By } from '@angular/platform-browser';
// import { Observable } from 'rxjs/Observable';
// import 'rxjs/add/observable/of';
// import 'rxjs/add/observable/throw';

import { Component, Directive } from '@angular/core';
import { RelationshipClosePhasesComponent } from './relationship-close-phases.component';
import { RelationshipService } from '../relationship.service';
import { Router } from '@angular/router';
import { SharedModule } from 'src/app/shared/shared.module';
import { Observable, of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

@Injectable()
class MockRelationshipService {
	getSystemsByClosePhase(closeId): Observable<any> {
		return of([ { id: 1 }, { id: 2 } ]);
	}
}

let router: Router;

@Injectable()
class MockRouter {
	navigate = jest.fn();
}

describe('RelationshipClosePhasesComponent', () => {
	let fixture;
	let component: RelationshipClosePhasesComponent;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [ SharedModule, RouterTestingModule.withRoutes([]) ],
			declarations: [ RelationshipClosePhasesComponent ],
			providers: [
				{ provide: RelationshipService, useClass: MockRelationshipService },
				{ provide: Router, useClass: MockRouter }
			],
			schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
		}).compileComponents();
		fixture = TestBed.createComponent(RelationshipClosePhasesComponent);
		component = fixture.debugElement.componentInstance;
		router = TestBed.get(Router);
	});

	it('should run #ngOnInit() and set init variables', async () => {
		component.ngOnInit();
		expect(component.selectedClosePhaseid).toBe(0);
	});

	it('should run #openSystemSelector()', async () => {
		const closePhaseId = 1;
		component.openSystemSelector(1);
		expect(component.selectedClosePhaseid).toBe(closePhaseId);
		expect(component.systems.length).toBe(2);
	});

	it('should run #openClosePhaseRelationship()', async () => {
		component.selectedSystem = { id: 1 };
		const spy = spyOn(router, 'navigate');

		component.openClosePhaseRelationship();

		expect(spy).toHaveBeenCalledWith([ 'relationship', 1 ]);
	});
});
