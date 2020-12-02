import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SessionService } from '../session/session.service';

const TOKEN_MSG = 'The Token has expired on';
@Injectable()
export class HttpConfigInterceptor implements HttpInterceptor {
  constructor(private router: Router, private msgSvc: MessageService, private sessionSvc: SessionService) {}
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token: string = this.sessionSvc.token;

    if (token) {
      request = request.clone({ headers: request.headers.set('Authorization', `Bearer ${token}`) });
    }

    if (!request.headers.has('Content-Type')) {
      request = request.clone({ headers: request.headers.set('Content-Type', 'application/json') });
    }

    request = request.clone({ headers: request.headers.set('Accept', 'application/json') });

    return next.handle(request).pipe(
      map((event: HttpEvent<any>) => {
        return event;
      }),
      catchError((error: HttpErrorResponse) => {
        this.msgSvc.clear('persist');
        if (error.status === 401 || (error.error && error.error.message === 'Forbidden')) {
          this.msgSvc.add({
            severity: 'error',
            summary: 'You shall not pass!',
            detail: 'You are not allowed to access that page',
            key: 'persist'
          });
          this.router.navigate(['/dashboard']);
          return;
        }
        if (error.status === 403) {
          this.msgSvc.add({
            severity: 'error',
            summary: 'Hold your horses!',
            detail: 'Session has expired',
            key: 'persist'
          });
          setTimeout(_ => {
            this.sessionSvc.logout();
          }, 3000);
        }

        if (error.status === 500 || error.status === 409) {
          if (error.error.message.startsWith(TOKEN_MSG)) {
            setTimeout(_ => {
              this.sessionSvc.logout();
            }, 2000);
          }
          this.msgSvc.add({
            severity: 'warn',
            summary: error.statusText,
            detail: error.error.message,
            key: 'persist'
          });
        }
        return throwError(error);
      })
    );
  }
}
