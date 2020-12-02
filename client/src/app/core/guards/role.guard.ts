import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { SessionService } from '../session/session.service';
import { MessageService } from 'primeng/api';

@Injectable()
export class RoleGuard implements CanActivate {
  constructor(private sessionSvc: SessionService, private msgSvc: MessageService) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const role = this.sessionSvc.role;
    const expectedRoles: any[] = route.data.expectedRoles;
    let result = false;
    if (expectedRoles.some(er => role === er)) {
      result = true;
    }
    if (result) {
      return true;
    } else {
      this.msgSvc.clear('persist');
      this.msgSvc.add({
        severity: 'error',
        summary: 'You shall not pass!',
        detail: 'You are not allowed to see that page',
        key: 'persist'
      });
      return false;
    }
  }
}
