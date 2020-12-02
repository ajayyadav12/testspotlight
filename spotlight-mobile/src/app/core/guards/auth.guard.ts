import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SessionService } from '../services/session.service';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private sessionSvc: SessionService) {}
  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    if (this.sessionSvc.token) {
      return true;
    } else {
      this.router.navigate(['/login']);
      return false;
    }
  }
}
