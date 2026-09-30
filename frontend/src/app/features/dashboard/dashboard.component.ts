import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { ProductResponse } from '../../data/models/product-response.model';
import { ProductService } from '../../data/services/product.service';
import { ProductTableComponent } from './product-table/product-table.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ProductTableComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  public readonly products = signal<ProductResponse[]>([]);
  public readonly loadState = signal<'loading' | 'loaded' | 'error'>('loading');

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);

  ngOnInit(): void {
    this.productService.findAll().subscribe({
      next: response => {
        this.products.set(response);
        this.loadState.set('loaded');
      },
      error: () => {
        this.loadState.set('error');
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }


}
