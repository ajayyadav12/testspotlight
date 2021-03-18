import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import {OverlayPanel} from 'primeng/overlaypanel';

@Injectable({
  providedIn: 'root'
})
export class GuidedTourService {
  // Guided Tour A
  a1: OverlayPanel;
  a2: OverlayPanel;
  a3: OverlayPanel;

  constructor(private route: Router) { }

  startGuidedTourA(){
    let gda1 = document.getElementById('GDA1');
    let gda2 = document.getElementById('GDA2');
    let gda3 = document.getElementById('GDA3');
     this.a1.toggle({target:gda1});
      this.a1.onHide.subscribe(_=>{
        this.route.navigate(['/relationship'])
        this.a2.toggle({target:gda2})
      })
      this.a2.onHide.subscribe(_=>{
        this.a3.toggle({target:gda3})
      })
  }
}
