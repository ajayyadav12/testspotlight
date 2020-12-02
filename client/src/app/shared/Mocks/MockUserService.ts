import { Observable, of } from 'rxjs';

export class MockUserService {
  getUsers(): Observable<any> {
    return of([{ id: 1, name: 'test', sso: '99999' }]);
  }
}
