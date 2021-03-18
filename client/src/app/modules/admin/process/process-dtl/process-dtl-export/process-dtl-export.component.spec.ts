import { ProcessDtlExportComponent } from './process-dtl-export.component';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Injectable } from '@angular/core';
import { ProcessService } from '../../process.service';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

@Injectable()
class MockProcessService {

}

describe('ProcessDtlExportComponent', () => {

    let fixture;
    let component: ProcessDtlExportComponent;

    const fakeActivatedRoute = {
        parent: {
            params: of({ id: 1 })
        }
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                PrimengModule,
                FormsModule
            ],
            declarations: [ProcessDtlExportComponent],
            providers: [{provide: ProcessService, useClass: MockProcessService }, { provide: ActivatedRoute, useFactory: () => fakeActivatedRoute }]
        }).compileComponents();
        fixture = TestBed.createComponent(ProcessDtlExportComponent);
        component = fixture.debugElement.componentInstance;
    });

    it('should create a component', async () => {
        expect(component).toBeTruthy();
    });

    it('should have only summary selected by default', async () => {
        expect(component.summary).toBeTruthy();
        expect(component.steps).toBeFalsy();
        expect(component.notifications).toBeFalsy();
        expect(component.users).toBeFalsy();
        expect(component.schedules).toBeFalsy();
    });

    it('should have import button disabled by default', async () => {
        expect(component.correctFormat).toBeFalsy();
    });

    it('should display approve dialog', async () => {
        component.approve(2);
        expect(component.display).toBeTruthy();
        expect(component.exportId).toEqual(2);
        expect(component.action).toEqual('Accept');
        expect(component.styleClass).toEqual('ui-button-success');
    });

    it('should display decline dialog', async () => {
        component.decline(3);
        expect(component.display).toBeTruthy();
        expect(component.exportId).toEqual(3);
        expect(component.action).toEqual('Decline');
        expect(component.styleClass).toEqual('ui-button-danger');
    });

});
