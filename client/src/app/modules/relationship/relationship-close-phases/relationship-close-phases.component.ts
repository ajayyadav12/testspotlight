import { Component, OnInit } from '@angular/core';
import { RelationshipService } from '../relationship.service';
import { Router } from '@angular/router';

@Component({
	selector: 'app-relationship-close-phases',
	templateUrl: './relationship-close-phases.component.html',
	styleUrls: [ './relationship-close-phases.component.scss' ]
})
export class RelationshipClosePhasesComponent implements OnInit {
	systems = [];
	selectedSystem;
	selectedClosePhaseid = 0;

	constructor(private relationshipSvc: RelationshipService, private router: Router) {}

	ngOnInit() {}

	openSystemSelector(closePhaseId: number) {
		this.selectedClosePhaseid = closePhaseId;
		this.relationshipSvc.getSystemsByClosePhase(closePhaseId).subscribe((value) => {
			this.systems = value;
		});
	}

	openClosePhaseRelationship() {
		this.router.navigate([ 'relationship', this.selectedSystem.id ]);
	}
}
