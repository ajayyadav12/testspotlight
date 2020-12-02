import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class ViewsService {
  constructor(private http: HttpClient) {}

  getViews(moduleName: string): Observable<any> {
    const url = `${environment.apiUrl}/module-filters/${moduleName}`;
    return this.http.get(url);
  }

  deleteView(id: number): Observable<any> {
    const url = `${environment.apiUrl}/module-filters/${id}`;
    return this.http.delete(url);
  }

  saveView(view): Observable<any> {
    const url = `${environment.apiUrl}/module-filters/`;
    return this.http.post(url, view);
  }
}
