import { Observable, of } from 'rxjs';

export class MockReceiverService {
  getAllReceiver(): Observable<any> {
    return of([{ id: 1, name: 'test' }]);
  }
}
