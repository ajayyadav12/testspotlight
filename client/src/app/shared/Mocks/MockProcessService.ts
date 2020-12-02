import { Observable, of } from 'rxjs';

export class MockProcessService {
  getAllProcesses(): Observable<any> {
    return of([
      {
        sender: { name: 'sender' },
        receiver: { name: 'receiver' },
        processType: { name: 'processType' }
      }
    ]);
  }
}
