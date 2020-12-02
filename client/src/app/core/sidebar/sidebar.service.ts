import { Injectable } from '@angular/core';
import { MENUITEMS } from './MENU_ITEMS';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  title = 'Spotlight';
  sidebarOpen = true;
  menuItems = [];

  constructor() {}

  reload(role) {
    this.menuItems = JSON.parse(JSON.stringify(MENUITEMS));
    this.menuItems.forEach(mp => {
      if (mp.items) {
        mp.items = mp.items.filter(m => {
          return !m.admin || (m.admin && role === 'admin');
        });
      }
    });
  }
}
