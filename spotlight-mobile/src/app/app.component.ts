import { Component, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { SessionService } from './core/services/session.service';

@Component({
  selector: 'ge-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnDestroy {
  title = 'Spotlight';
  mobileQuery: MediaQueryList;
  private _mobileQueryListener: () => void;
  navMenu = [{ title: 'Submissions', router: '/submissions' }];

  constructor(changeDetectorRef: ChangeDetectorRef, media: MediaMatcher, public sessionSvc: SessionService) {
    this.mobileQuery = media.matchMedia('(max-width: 600px)');
    this._mobileQueryListener = () => changeDetectorRef.detectChanges();
    this.mobileQuery.addListener(this._mobileQueryListener);
  }

  ngOnDestroy(): void {
    this.mobileQuery.removeListener(this._mobileQueryListener);
  }

  onClickLogOut(){
    this.sessionSvc.logout();
  }
}
