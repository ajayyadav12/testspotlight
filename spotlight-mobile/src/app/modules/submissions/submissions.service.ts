import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable()
export class SubmissionsService {
  constructor(private http: HttpClient) {}

  getSubmissions(params?): Observable<any> {
    const url = environment.apiUrl + '/submissions/';
    return this.http.get(url, {
      params: params
    });
  }

  getSubmissionSteps(id: number): Observable<any> {
    const url = `${environment.apiUrl}/submissions/${id}/steps`;
    return this.http.get(url);
  }

  submissionStatusColor(status): string {
    let color;
    switch (status) {
      case 'success':
        color = '#00bf6f';
        break;
      case 'in progress':
        color = '#027ad9';
        break;
      case 'warning':
        color = '#ffa600';
        break;
      case 'failed':
        color = '#f44336';
        break;
      default:
        break;
    }
    return color;
  }
}
