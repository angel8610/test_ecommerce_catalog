import { CurrencyPipe, SlicePipe } from '@angular/common';
import { Component, computed, input, signal } from '@angular/core';

import { ProductResponse } from '../../../data/models/product-response.model';

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [CurrencyPipe, SlicePipe],
  templateUrl: './product-table.component.html',
  styleUrl: './product-table.component.css'
})
export class ProductTableComponent {
  public readonly products = input.required<ProductResponse[]>();
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
    if(target instanceof HTMLSelectElement) {
      this.selectedCategory.set(target.value);
    }
  }


}
