import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { AuditLogResponse } from '../../../../data/models/audit-log-response.model';

@Component({
  selector: 'app-audit-log-table',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './audit-log-table.component.html',
  styleUrl: './audit-log-table.component.css'
})
export class AuditLogTableComponent {

  public readonly auditLogs = input.required<AuditLogResponse[]>();


}
