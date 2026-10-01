import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of } from 'rxjs';

import { AuditLogResponse } from '../../../data/models/audit-log-response.model';
import { AuditLogService } from '../../../data/services/audit-log.service';
import { AuditLogPageComponent } from './audit-log-page.component';

const AUDIT_LOG: AuditLogResponse = {
  auditLogId: 17,
  operation: 'CREATE_PRODUCT',
  status: 'SUCCESS',
  durationMs: 42,
  registerDate: new Date('2026-09-30T12:00:00Z'),
  createdBy: 'admin',
  error: ''
};

describe('AuditLogPageComponent', () => {
  let fixture: ComponentFixture<AuditLogPageComponent>;
  let responses: Observable<AuditLogResponse[]>[];
  let requestCount: number;

  beforeEach(async () => {
    responses = [];
    requestCount = 0;

    await TestBed.configureTestingModule({
      imports: [AuditLogPageComponent],
      providers: [{
        provide: AuditLogService,
        useValue: {
          findAll: () => responses[requestCount++]
        }
      }]
    }).compileComponents();

    fixture = TestBed.createComponent(AuditLogPageComponent);
    fixture.detectChanges();
  });

  it('stays idle until refreshKey changes, then loads and displays records', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(fixture.componentInstance.loadState()).toBe('idle');
    expect(requestCount).toBe(0);
    expect(element.querySelector('app-audit-log-table')).toBeNull();

    fixture.componentRef.setInput('refreshKey', 0);
    fixture.detectChanges();
    expect(fixture.componentInstance.loadState()).toBe('idle');
    expect(requestCount).toBe(0);

    const response = new Subject<AuditLogResponse[]>();
    responses.push(response);
    fixture.componentRef.setInput('refreshKey', 1);
    fixture.detectChanges();

    expect(fixture.componentInstance.loadState()).toBe('loading');
    expect(requestCount).toBe(1);
    expect(element.querySelector('[role="status"]')?.textContent)
      .toContain('Cargando registros de auditoría');

    response.next([AUDIT_LOG]);
    fixture.detectChanges();
    expect(fixture.componentInstance.loadState()).toBe('loaded');
    expect(element.querySelector('tbody')?.textContent).toContain('CREATE_PRODUCT');
    expect(element.querySelector('tbody')?.textContent).toContain('admin');

    const refreshedResponse = new Subject<AuditLogResponse[]>();
    responses.push(refreshedResponse);
    fixture.componentRef.setInput('refreshKey', 2);
    fixture.detectChanges();
    expect(requestCount).toBe(2);
    expect(fixture.componentInstance.loadState()).toBe('loading');

    refreshedResponse.next([{ ...AUDIT_LOG, operation: 'UPDATE_PRODUCT' }]);
    fixture.detectChanges();
    expect(element.querySelector('tbody')?.textContent).toContain('UPDATE_PRODUCT');
  });

  it('shows an error, retries, and renders the empty state when no records remain', () => {
    const failedResponse = new Subject<AuditLogResponse[]>();
    responses.push(failedResponse);
    fixture.componentRef.setInput('refreshKey', 0);
    fixture.detectChanges();
    fixture.componentRef.setInput('refreshKey', 1);
    fixture.detectChanges();
    failedResponse.error(new Error('Request failed'));
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(fixture.componentInstance.loadState()).toBe('error');
    expect(element.querySelector('[role="alert"]')?.textContent)
      .toContain('No se pudieron cargar los registros de auditoría');

    responses.push(of([]));
    element.querySelector<HTMLButtonElement>('.btn-outline-danger')?.click();
    fixture.detectChanges();

    expect(requestCount).toBe(2);
    expect(fixture.componentInstance.loadState()).toBe('loaded');
    expect(element.querySelector('[role="status"]')?.textContent)
      .toContain('No hay registros de auditoría disponibles');
    expect(element.querySelector('app-audit-log-table')).toBeNull();
  });
});