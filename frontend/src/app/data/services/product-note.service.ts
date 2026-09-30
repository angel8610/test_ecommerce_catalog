import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {ProductNoteResponse} from '../models/product-note-response.model';
import {ProductNoteRequest} from '../models/product-note-request.model';

@Injectable({
  providedIn: 'root'
})
export class ProductNoteService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/prodnotes`;

  save(productNoteRequest: ProductNoteRequest): Observable<ProductNoteResponse> {
    return this.http.post<ProductNoteResponse>(this.apiUrl, productNoteRequest);
  }

  findAll(): Observable<ProductNoteResponse[]> {
    return this.http.get<ProductNoteResponse[]>(this.apiUrl);
  }


}
