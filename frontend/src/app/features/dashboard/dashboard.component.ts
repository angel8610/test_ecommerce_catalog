import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { AuditLogPageComponent } from './audit-log/audit-log-page.component';
import { ProductCatalogComponent } from './product-catalog/product-catalog.component';

type DashboardView = 'products' | 'audit';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [AuditLogPageComponent, ProductCatalogComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  public readonly selectedView = signal<DashboardView>('products');
  public readonly auditRefreshKey = signal(0);

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  selectView(view: DashboardView): void {
    this.selectedView.set(view);
    if(view === 'audit') {
      this.auditRefreshKey.update(key => key + 1);
    }
  }


}
