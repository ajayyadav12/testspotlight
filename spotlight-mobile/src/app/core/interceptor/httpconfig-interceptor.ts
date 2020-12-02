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
import { SessionService } from '../services/session.service';

const TOKEN_MSG = 'The Token has expired on';
@Injectable()
export class HttpConfigInterceptor implements HttpInterceptor {
  constructor(private router: Router, private sessionSvc: SessionService) {}
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
        if (error.status === 401 || (error.error && error.error.message === 'Forbidden')) {
          this.router.navigate(['/']);
          return;
        }
        if (error.status === 403) {
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
        }
        return throwError(error);
      })
    );
  }
}
