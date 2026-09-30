import { CurrencyPipe } from '@angular/common';
import { Component, input, output, signal } from '@angular/core';

import { ProductResponse } from '../../../data/models/product-response.model';

export interface ProductNoteRegistration {
  product: ProductResponse;
  note: string;
}

@Component({
  selector: 'app-product-note-modal',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './product-note-modal.component.html'
})
export class ProductNoteModalComponent {

  public readonly product = input.required<ProductResponse>();
  public readonly saving = input(false);
  public readonly errorMessage = input('');
  public readonly cancel = output<void>();
  public readonly register = output<ProductNoteRegistration>();
  public readonly note = signal('');

  public onChangeNote(event: Event): void {
    const target = event.target;
    if(target instanceof HTMLInputElement) {
      this.note.set(target.value);
    }
  }

  public registerNote(): void {
    if(this.saving() || !this.note().trim()) {
      return;
    }

    this.register.emit({ product: this.product(), note: this.note() });
  }


}
