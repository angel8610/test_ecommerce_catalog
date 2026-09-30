import { CurrencyPipe, SlicePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { ProductNoteRequest } from '../../../data/models/product-note-request.model';
import { ProductNoteResponse } from '../../../data/models/product-note-response.model';
import { ProductResponse } from '../../../data/models/product-response.model';
import { ProductNoteService } from '../../../data/services/product-note.service';
import { ProductNoteModalComponent, ProductNoteRegistration } from './product-note-modal.component';

const CREATED_BY = 'admin';

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [CurrencyPipe, SlicePipe, ProductNoteModalComponent],
  templateUrl: './product-table.component.html',
  styleUrl: './product-table.component.css'
})
export class ProductTableComponent {
  private readonly productNoteService = inject(ProductNoteService);

  public readonly products = input.required<ProductResponse[]>();
  public readonly selectedProduct = signal<ProductResponse | null>(null);
  public readonly savingNote = signal(false);
  public readonly noteSaveError = signal('');
  public readonly noteSaveSuccess = signal('');

  private readonly savedNotes = signal<Record<number, string>>({});
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

  public openModal(product: ProductResponse): void {
    if (this.savingNote()) {
      return;
    }
    this.noteSaveError.set('');
    this.noteSaveSuccess.set('');
    this.selectedProduct.set(product);
  }

  public closeModal(): void {
    if (this.savingNote()) {
      return;
    }
    this.selectedProduct.set(null);
  }

  public noteFor(product: ProductResponse): string {
    return this.savedNotes()[product.id] ?? product.note;
  }

  public onNoteRegister(registration: ProductNoteRegistration): void {
    const { product, note } = registration;
    const trimNote = note.trim();

    if(this.savingNote() || !trimNote) {
      return;
    }

    const request: ProductNoteRequest = {
      extProdId: product.id,
      note: trimNote,
      createdBy: CREATED_BY
    };

    this.noteSaveError.set('');
    this.noteSaveSuccess.set('');
    this.savingNote.set(true);
    this.productNoteService.save(request).pipe(finalize(() => this.savingNote.set(false)))
      .subscribe({
        next: (response: ProductNoteResponse) => {
          this.savedNotes.update(notes => ({ ...notes, [product.id]: response.note }));
          this.noteSaveSuccess.set(`La nota para "${product.title}" se guardó correctamente.`);
          this.selectedProduct.set(null);
        }, error: (error: unknown) => this.noteSaveError.set(this.getNoteSaveError(error))
      });
  }

  private getNoteSaveError(error: unknown): string {
    if(error instanceof HttpErrorResponse) {
      const responseMessage = this.getResponseMessage(error.error);
      if(responseMessage) {
        return responseMessage;
      }

      if(error.status === 0) {
        return 'No se pudo conectar con el servidor. Revisá tu conexión e intentá nuevamente.';
      }

      if(error.status === 400) {
        return 'No se pudo registrar la nota. Usá solo letras y espacios, con un máximo de 1000 caracteres.';
      }

      if(error.status === 401 || error.status === 403) {
        return 'No tenés permisos para registrar notas. Iniciá sesión con una cuenta administradora.';
      }

      return `No se pudo registrar la nota (HTTP ${error.status}). Intentá nuevamente.`;
    }

    return 'No se pudo registrar la nota por un error inesperado. Intentá nuevamente.';
  }

  private getResponseMessage(body: unknown): string | null {
    if(typeof body === 'string' && body.trim()) {
      return body;
    }

    if(typeof body === 'object' && body !== null) {
      for(const key of ['detail', 'message']) {
        const value = Reflect.get(body, key);
        if(typeof value === 'string' && value.trim()) {
          return value;
        }
      }
    }

    return null;
  }


}
