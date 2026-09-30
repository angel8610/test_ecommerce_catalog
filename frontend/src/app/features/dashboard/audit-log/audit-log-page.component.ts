import { Component, Input, inject, signal } from '@angular/core';

import { AuditLogResponse } from '../../../data/models/audit-log-response.model';
import { AuditLogService } from '../../../data/services/audit-log.service';
import { AuditLogTableComponent } from './audit-log-table/audit-log-table.component';

type AuditLoadState = 'idle' | 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-audit-log-page',
  standalone: true,
  imports: [AuditLogTableComponent],
  templateUrl: './audit-log-page.component.html'
})
export class AuditLogPageComponent {
  public readonly auditLogs = signal<AuditLogResponse[]>([]);
  public readonly loadState = signal<AuditLoadState>('idle');

  private readonly auditLogService = inject(AuditLogService);
  private lastRefreshKey: number | null = null;

  @Input()
  set refreshKey(value: number) {
    if (this.lastRefreshKey !== null && value !== this.lastRefreshKey) {
      this.loadAuditLogs();
    }
    this.lastRefreshKey = value;
  }

  public loadAuditLogs(): void {
    if (this.loadState() === 'loading') {
      return;
    }

    this.loadState.set('loading');
    this.auditLogService.findAll().subscribe({
      next: response => {
        this.auditLogs.set(response);
        this.loadState.set('loaded');
      },
      error: () => this.loadState.set('error')
    });
  }


}
