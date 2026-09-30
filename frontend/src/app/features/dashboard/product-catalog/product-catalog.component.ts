import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { ProductNoteRequest } from '../../../data/models/product-note-request.model';
import { ProductNoteResponse } from '../../../data/models/product-note-response.model';
import { ProductResponse } from '../../../data/models/product-response.model';
import { ProductNoteService } from '../../../data/services/product-note.service';
import { ProductService } from '../../../data/services/product.service';
import { ProductNoteRegistration } from './product-catalog-modal/product-note-modal.component';
import { ProductTableComponent } from './product-catalog-table/product-table.component';

type CatalogLoadState = 'loading' | 'loaded' | 'error';

const CREATED_BY = 'admin';// For a limited time

@Component({
  selector: 'app-product-catalog',
  standalone: true,
  imports: [ProductTableComponent],
  templateUrl: './product-catalog.component.html'
})
export class ProductCatalogComponent implements OnInit {

  public readonly products = signal<ProductResponse[]>([]);
  public readonly loadState = signal<CatalogLoadState>('loading');
  public readonly selectedProduct = signal<ProductResponse | null>(null);
  public readonly savingNote = signal(false);
  public readonly noteSaveError = signal('');
  public readonly noteSaveSuccess = signal('');

  private readonly productService = inject(ProductService);
  private readonly productNoteService = inject(ProductNoteService);

  ngOnInit(): void {
    this.loadProducts();
  }

  public openNote(product: ProductResponse): void {
    if (this.savingNote() || product.note.trim()) {
      return;
    }

    this.noteSaveError.set('');
    this.noteSaveSuccess.set('');
    this.selectedProduct.set(product);
  }

  public closeNote(): void {
    if (!this.savingNote()) {
      this.selectedProduct.set(null);
    }
  }

  public registerNote(registration: ProductNoteRegistration): void {
    const product = this.selectedProduct();
    const note = registration.note.trim();

    if (this.savingNote() || !product) {
      return;
    }

    if (!note) {
      this.noteSaveError.set('La nota es obligatoria.');
      return;
    }

    this.noteSaveError.set('');
    this.noteSaveSuccess.set('');
    this.savingNote.set(true);

    const request: ProductNoteRequest = {
      extProdId: product.id,
      note,
      createdBy: CREATED_BY
    };

    this.productNoteService.save(request)
      .pipe(finalize(() => this.savingNote.set(false)))
      .subscribe({
        next: (response: ProductNoteResponse) => {
          this.products.update(products => products.map(current =>
            current.id === product.id ? { ...current, note: response.note } : current
          ));
          this.noteSaveSuccess.set(`La nota para "${product.title}" se guard\u00f3 correctamente.`);
          this.selectedProduct.set(null);
        },
        error: (error: unknown) => this.noteSaveError.set(this.getNoteSaveError(error))
      });
  }

  private loadProducts(): void {
    this.loadState.set('loading');
    this.productService.findAll().subscribe({
      next: response => {
        this.products.set(response);
        this.loadState.set('loaded');
      },
      error: () => this.loadState.set('error')
    });
  }

  private getNoteSaveError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const responseMessage = this.getResponseMessage(error.error);
      if (responseMessage) {
        return responseMessage;
      }

      if (error.status === 0) {
        return 'No se pudo conectar con el servidor. Revis\u00e1 tu conexi\u00f3n e intent\u00e1 nuevamente.';
      }

      if (error.status === 400) {
        return 'No se pudo registrar la nota. Us\u00e1 solo letras y espacios, con un m\u00e1ximo de 1000 caracteres.';
      }

      if (error.status === 401 || error.status === 403) {
        return 'No ten\u00e9s permisos para registrar notas. Inici\u00e1 sesi\u00f3n con una cuenta administradora.';
      }

      return `No se pudo registrar la nota (HTTP ${error.status}). Intent\u00e1 nuevamente.`;
    }

    return 'No se pudo registrar la nota por un error inesperado. Intent\u00e1 nuevamente.';
  }

  private getResponseMessage(body: unknown): string | null {
    if (typeof body === 'string' && body.trim()) {
      return body;
    }

    if (typeof body === 'object' && body !== null) {
      for (const key of ['detail', 'message']) {
        const value = Reflect.get(body, key);
        if (typeof value === 'string' && value.trim()) {
          return value;
        }
      }
    }

    return null;
  }


}
