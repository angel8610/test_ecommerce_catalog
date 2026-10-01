import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';

import { ProductNoteRequest } from '../../../data/models/product-note-request.model';
import { ProductNoteResponse } from '../../../data/models/product-note-response.model';
import { ProductResponse } from '../../../data/models/product-response.model';
import { ProductNoteService } from '../../../data/services/product-note.service';
import { ProductService } from '../../../data/services/product.service';
import { ProductCatalogComponent } from './product-catalog.component';

const PRODUCT: ProductResponse = {
  id: 7,
  title: 'Test product',
  price: 12.5,
  description: 'Test description',
  category: 'Test category',
  rating: { rate: 4, count: 5 },
  note: ''
};

const SAVED_NOTE: ProductNoteResponse = {
  noteId: 1,
  extProdId: PRODUCT.id,
  note: 'Server saved note',
  createdBy: 'admin'
};

describe('ProductCatalogComponent', () => {
  let fixture: ComponentFixture<ProductCatalogComponent>;
  let component: ProductCatalogComponent;
  let productResponse: Observable<ProductResponse[]>;
  let saveResponse: Observable<ProductNoteResponse>;
  let requests: ProductNoteRequest[];

  beforeEach(async () => {
    productResponse = of([PRODUCT]);
    saveResponse = of(SAVED_NOTE);
    requests = [];

    await TestBed.configureTestingModule({
      imports: [ProductCatalogComponent],
      providers: [
        {
          provide: ProductService,
          useValue: { findAll: () => productResponse }
        },
        {
          provide: ProductNoteService,
          useValue: {
            save: (request: ProductNoteRequest): Observable<ProductNoteResponse> => {
              requests.push(request);
              return saveResponse;
            }
          }
        }
      ]
    }).compileComponents();
  });

  it('loads and displays the product catalog', () => {
    createFixture();

    expect(component.loadState()).toBe('loaded');
    expect(component.products()).toEqual([PRODUCT]);
    expect((fixture.nativeElement as HTMLElement).querySelector('tbody')?.textContent)
      .toContain(PRODUCT.title);
  });

  it('shows an explicit error when catalog retrieval fails', () => {
    productResponse = throwError(() => new Error('Request failed'));
    createFixture();

    expect(component.loadState()).toBe('error');
    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent)
      .toContain('No se pudieron cargar los productos');
  });

  it('persists a note, closes the modal, and updates the product row on success', () => {
    createFixture();
    enterNote(' Useful note ');
    submitNote();

    expect(requests).toEqual([{
      extProdId: PRODUCT.id,
      note: 'Useful note',
      createdBy: 'admin'
    }]);
    expect(component.selectedProduct()).toBeNull();
    expect(component.products()[0].note).toBe(SAVED_NOTE.note);
    expect(component.noteSaveSuccess()).toBe('La nota para "Test product" se guardó correctamente.');

    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.alert-success')?.textContent).toContain('se guardó correctamente');
    expect(element.querySelector('tbody tr')?.textContent).toContain(SAVED_NOTE.note);
    expect(element.querySelector<HTMLButtonElement>('.note-action-button')?.disabled).toBe(true);
  });

  it('keeps the modal and entered note visible when saving fails', () => {
    saveResponse = throwError(
      () => new HttpErrorResponse({ status: 400, error: { error: 'Bad Request' } })
    );
    createFixture();
    enterNote('Useful note');
    submitNote();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(component.selectedProduct()).toBe(PRODUCT);
    expect(element.querySelector<HTMLInputElement>('#productNote')?.value).toBe('Useful note');
    expect(element.querySelector('[role="alert"]')?.textContent)
      .toContain('solo letras y espacios');
  });

  it('closes the note modal on cancel without sending a request', () => {
    createFixture();
    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.note-action-button')?.click();
    fixture.detectChanges();

    element.querySelector<HTMLButtonElement>('.modal-footer .btn-secondary')?.click();
    fixture.detectChanges();

    expect(component.selectedProduct()).toBeNull();
    expect(requests).toEqual([]);
  });

  it('deduplicates submissions while a note save is in progress', () => {
    const pendingSave = new Subject<ProductNoteResponse>();
    saveResponse = pendingSave;
    createFixture();
    enterNote('Useful note');
    submitNote();

    const element = fixture.nativeElement as HTMLElement;
    expect(component.savingNote()).toBe(true);
    expect(element.querySelector<HTMLButtonElement>('.modal-footer .btn-primary')?.disabled).toBe(true);
    expect(element.querySelector<HTMLButtonElement>('.modal-footer .btn-secondary')?.disabled).toBe(true);

    component.registerNote({ product: PRODUCT, note: 'Useful note' });
    expect(requests).toHaveLength(1);

    pendingSave.error(new HttpErrorResponse({ status: 500 }));
  });

  function createFixture(): void {
    fixture = TestBed.createComponent(ProductCatalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function enterNote(note: string): void {
    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.note-action-button')?.click();
    fixture.detectChanges();
    const input = element.querySelector<HTMLInputElement>('#productNote')!;
    input.value = note;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  function submitNote(): void {
    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('.modal-footer .btn-primary')?.click();
    fixture.detectChanges();
  }
});
