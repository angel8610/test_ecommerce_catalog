import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductResponse } from '../../../../data/models/product-response.model';
import { ProductTableComponent } from './product-table.component';

const PRODUCT: ProductResponse = {
  id: 7,
  title: 'Test product',
  price: 12.5,
  description: 'a'.repeat(155),
  category: 'Test category',
  rating: { rate: 4, count: 5 },
  note: ''
};

const PRODUCT_WITH_NOTE: ProductResponse = {
  ...PRODUCT,
  id: 8,
  title: 'Noted product',
  category: 'Other category',
  note: 'Existing note'
};

describe('ProductTableComponent', () => {
  let fixture: ComponentFixture<ProductTableComponent>;
  let component: ProductTableComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductTableComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductTableComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('products', [PRODUCT, PRODUCT_WITH_NOTE]);
    fixture.detectChanges();
  });

  it('filters categories, truncates long descriptions, and disables notes already present', () => {
    const element = fixture.nativeElement as HTMLElement;
    const rows = element.querySelectorAll<HTMLTableRowElement>('tbody tr');

    expect(rows).toHaveLength(2);
    expect(rows[0].cells[1].textContent?.trim()).toHaveLength(153);
    expect(rows[0].cells[1].textContent?.trim().endsWith('...')).toBe(true);
    expect(rows[1].querySelector('.note-action-button')?.hasAttribute('disabled')).toBe(true);

    const category = element.querySelector<HTMLSelectElement>('#productCategory')!;
    category.value = 'Other category';
    category.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(element.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(element.querySelector('tbody')?.textContent).toContain('Noted product');
  });

  it('requires a non-whitespace note before registration', () => {
    fixture.componentRef.setInput('selectedProduct', PRODUCT);
    fixture.detectChanges();

    const registrations: { product: ProductResponse; note: string }[] = [];
    component.noteRegistration.subscribe(registration => registrations.push(registration));
    const element = fixture.nativeElement as HTMLElement;
    const noteInput = element.querySelector<HTMLInputElement>('#productNote')!;
    noteInput.value = '   ';
    noteInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const registerButton = element.querySelector<HTMLButtonElement>('.modal-footer .btn-primary')!;
    expect(registerButton.disabled).toBe(true);
    registerButton.click();
    expect(registrations).toEqual([]);
  });

  it('emits typed note requests, cancellation, and registration events', () => {
    const requested: ProductResponse[] = [];
    const registrations: { product: ProductResponse; note: string }[] = [];
    let cancelCount = 0;
    component.noteRequested.subscribe(product => requested.push(product));
    component.noteRegistration.subscribe(registration => registrations.push(registration));
    component.cancelNote.subscribe(() => cancelCount += 1);

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.note-action-button')?.click();
    expect(requested).toEqual([PRODUCT]);

    fixture.componentRef.setInput('selectedProduct', PRODUCT);
    fixture.detectChanges();
    expect(element.querySelector('.modal-body')?.textContent).toContain('ID del producto');
    expect(element.querySelector('.modal-body')?.textContent).toContain('7');

    const noteInput = element.querySelector<HTMLInputElement>('#productNote')!;
    noteInput.value = ' New note ';
    noteInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    element.querySelector<HTMLButtonElement>('.modal-footer .btn-primary')?.click();
    element.querySelector<HTMLButtonElement>('.modal-footer .btn-secondary')?.click();

    expect(registrations).toEqual([{ product: PRODUCT, note: ' New note ' }]);
    expect(cancelCount).toBe(1);
  });
});