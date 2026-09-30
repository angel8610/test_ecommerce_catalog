import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {AuditLogResponse} from '../models/audit-log-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/auditlogs`;

  findAll(): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>(this.apiUrl);
  }


}
