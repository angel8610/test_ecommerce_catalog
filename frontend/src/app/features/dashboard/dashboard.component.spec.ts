import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, Subject, of } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { AuditLogResponse } from '../../data/models/audit-log-response.model';
import { ProductNoteResponse } from '../../data/models/product-note-response.model';
import { ProductNoteService } from '../../data/services/product-note.service';
import { ProductService } from '../../data/services/product.service';
import { AuditLogService } from '../../data/services/audit-log.service';
import { DashboardComponent } from './dashboard.component';

const AUDIT_LOG: AuditLogResponse = {
  auditLogId: 17,
  operation: 'CREATE_PRODUCT',
  status: 'SUCCESS',
  durationMs: 42,
  registerDate: new Date('2026-09-30T12:00:00Z'),
  createdBy: 'admin',
  error: ''
};

const NOTE_RESPONSE: ProductNoteResponse = {
  noteId: 1,
  extProdId: 1,
  note: 'Test note',
  createdBy: 'admin'
};

describe('DashboardComponent navigation', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let auditResponse: Observable<AuditLogResponse[]>;
  let auditRequests: number;

  beforeEach(async () => {
    auditRequests = 0;
    auditResponse = new Subject<AuditLogResponse[]>();

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: AuthService, useValue: { logout: () => undefined } },
        { provide: Router, useValue: { navigate: () => Promise.resolve(true) } },
        { provide: ProductService, useValue: { findAll: () => of([]) } },
        { provide: ProductNoteService, useValue: { save: () => of(NOTE_RESPONSE) } },
        {
          provide: AuditLogService,
          useValue: {
            findAll: () => {
              auditRequests += 1;
              return auditResponse;
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('refreshes audit records on each selection while deduplicating concurrent requests', () => {
    const element = fixture.nativeElement as HTMLElement;
    const buttons = element.querySelectorAll<HTMLButtonElement>('.nav-link');

    expect(auditRequests).toBe(0);
    expect(buttons[0].getAttribute('aria-current')).toBe('page');
    expect(buttons[1].getAttribute('aria-current')).toBeNull();

    buttons[1].click();
    fixture.detectChanges();
    expect(auditRequests).toBe(1);
    expect(element.querySelector('#audit-panel [role="status"]')?.textContent)
      .toContain('Cargando registros de auditoría');

    buttons[1].click();
    fixture.detectChanges();
    expect(auditRequests).toBe(1);

    (auditResponse as Subject<AuditLogResponse[]>).next([AUDIT_LOG]);
    fixture.detectChanges();
    expect(element.querySelector('tbody')?.textContent).toContain('CREATE_PRODUCT');
    expect(element.querySelector('tbody')?.textContent).toContain('admin');
    expect(element.querySelector('tbody')?.textContent).toContain('42');

    buttons[0].click();
    fixture.detectChanges();
    auditResponse = of([{ ...AUDIT_LOG, operation: 'CREATE_NOTE' }]);
    buttons[1].click();
    fixture.detectChanges();
    expect(auditRequests).toBe(2);
    expect(element.querySelector('tbody')?.textContent).toContain('CREATE_NOTE');
  });

  it('shows an error and lets the user retry the audit request', () => {
    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('.nav-link')[1].click();
    (auditResponse as Subject<AuditLogResponse[]>).error(new Error('Request failed'));
    fixture.detectChanges();

    expect(element.querySelector('#audit-panel [role="alert"]')?.textContent)
      .toContain('No se pudieron cargar los registros de auditoría');

    auditResponse = of([AUDIT_LOG]);
    element.querySelector<HTMLButtonElement>('.btn-outline-danger')?.click();
    fixture.detectChanges();
    expect(auditRequests).toBe(2);
    expect(element.querySelector('tbody')?.textContent).toContain('CREATE_PRODUCT');
  });

  it('shows an empty state when no audit records are returned', () => {
    auditResponse = of([]);
    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('.nav-link')[1].click();
    fixture.detectChanges();

    expect(element.querySelector('#audit-panel [role="status"]')?.textContent)
      .toContain('No hay registros de auditoría disponibles');
  });
});