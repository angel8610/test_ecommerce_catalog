import { CurrencyPipe, SlicePipe } from '@angular/common';
import { Component, computed, input, output, signal } from '@angular/core';

import { ProductResponse } from '../../../../data/models/product-response.model';
import {
  ProductNoteModalComponent,
  ProductNoteRegistration
} from '../product-catalog-modal/product-note-modal.component';

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [CurrencyPipe, SlicePipe, ProductNoteModalComponent],
  templateUrl: './product-table.component.html',
  styleUrl: './product-table.component.css'
})
export class ProductTableComponent {

  public readonly products = input.required<ProductResponse[]>();
  public readonly selectedProduct = input<ProductResponse | null>(null);
  public readonly savingNote = input(false);
  public readonly noteSaveError = input('');
  public readonly noteSaveSuccess = input('');
  public readonly noteRequested = output<ProductResponse>();
  public readonly cancelNote = output<void>();
  public readonly noteRegistration = output<ProductNoteRegistration>();
  public readonly selectedCategory = signal('');
  public readonly categories = computed(() =>
    [...new Set(this.products().map(product => product.category))].sort((a, b) =>
      a.localeCompare(b)
    )
  );
  public readonly filteredProducts = computed(() => {
    const category = this.selectedCategory();
    return category
      ? this.products().filter(product => product.category === category)
      : this.products();
  });

  public onCategoryChange(event: Event): void {
    const target = event.target;
    if (target instanceof HTMLSelectElement) {
      this.selectedCategory.set(target.value);
    }
  }

  public hasNote(product: ProductResponse): boolean {
    return product.note.trim().length > 0;
  }


}
