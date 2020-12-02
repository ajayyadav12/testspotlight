import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-ge-table',
  templateUrl: './ge-table.component.html',
  styleUrls: ['./ge-table.component.scss']
})
export class GeTableComponent implements OnInit {
  @Input() isLightVersion;
  @Input() value: any[];
  @Input() columns: any[];
  @Input() routerLink: string;
  @Input() showEditDelete = true;
  @Input() loading = false;
  @Input() menuItems = [];
  @Input() iconColumn = '';
  @Input() rows = 10;
  @Output() deleteRecord = new EventEmitter();
  @Output() clickAction = new EventEmitter();
  @Output() clickMenuOption = new EventEmitter();

  _menuItems = [];
  recordSelected;

  constructor(private router: Router) {}

  ngOnInit() {
    this.menuItems.forEach(item => {
      this._menuItems.push({
        label: item.label,
        icon: item.icon,
        command: event => {
          this.clickMenuOption.emit({ item: event.item.label, value: this.recordSelected });
        }
      });
    });
  }

  clickItem(row) {
    this.clickAction.emit(row);
  }

  deleteItem(id) {
    if (confirm('Are you sure you want to delete this record?')) {
      this.deleteRecord.emit(id);
    }
  }

  editItem(id) {
    if (this.routerLink) {
      this.router.navigate([this.routerLink + id]);
    } else {
      alert('Edit record is not enabled');
    }
  }
}
