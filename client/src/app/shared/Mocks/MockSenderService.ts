import { Observable, of } from 'rxjs';

export class MockSenderService {
  getSender(id): Observable<any> {
    return of({ id: 1, name: 'test', appOwner: { id: 1 } });
  }

  getAllSenders(): Observable<any> {
    return of([{ id: 1, name: 'test', appOwner: { id: 1 } }]);
  }

  newSender(name): Observable<any> {
    return of({
      id: 1,
      name: 'New User'
    });
  }
}
