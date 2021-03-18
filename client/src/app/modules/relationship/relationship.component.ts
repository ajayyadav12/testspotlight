import { Component, OnInit } from '@angular/core';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';

@Component({
  selector: 'app-relationship',
  templateUrl: './relationship.component.html',
  styleUrls: ['./relationship.component.scss']
})
export class RelationshipComponent implements OnInit {

  constructor(private sidebarSvc: SidebarService) { }

  ngOnInit() {
    this.sidebarSvc.title = 'Relationship Views';
  }

}
