import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Product } from './product.model';

@Component({
  selector: 'app-operations-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './operations-page.html',
  styleUrl: './operations-page.scss'
})
export class OperationsPage implements OnInit {
  private readonly apiUrl = '/api/v1/products';
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(true);
  protected readonly processing = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected selectedId = '';
  protected quantity = 1;
  protected productSearch = '';
  protected operation: 'sale' | 'restock' = 'sale';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<Product[]>(this.apiUrl).subscribe({
      next: (products) => { this.products.set(products); this.selectedId = products[0]?.id ?? ''; this.loading.set(false); },
      error: (response) => { this.showError(response, 'Nao foi possivel carregar os produtos.'); this.loading.set(false); }
    });
  }

  protected get selectedProduct(): Product | undefined { return this.products().find((product) => product.id === this.selectedId); }
    protected get filteredProducts(): Product[] {
      const term = this.productSearch.trim().toLowerCase();
      return this.products().filter((product) => product.name.toLowerCase().includes(term));
    }
    protected get saleTotal(): number { return (this.selectedProduct?.price ?? 0) * (this.quantity || 0); }
    protected selectProduct(product: Product): void { this.selectedId = product.id; this.error.set(''); }
    protected dismissError(): void { this.error.set(''); }
  protected setOperation(operation: 'sale' | 'restock'): void { this.operation = operation; this.message.set(''); this.error.set(''); }

  protected submit(): void {
    const requestedQuantity = Number(this.quantity);
    if (!this.selectedId || !Number.isInteger(requestedQuantity) || requestedQuantity < 1) { this.error.set('Informe uma quantidade inteira maior que zero.'); return; }
    if (this.operation === 'sale' && this.selectedProduct && requestedQuantity > this.selectedProduct.quantity) { this.error.set('A quantidade informada supera o estoque disponivel.'); return; }
    this.processing.set(true); this.message.set(''); this.error.set('');
    const endpoint = `${this.apiUrl}/${this.selectedId}/${this.operation}`;
    const request = this.operation === 'sale'
      ? this.http.post<Product>(endpoint, { quantity: requestedQuantity })
      : this.http.put<Product>(endpoint, { quantity: requestedQuantity });
    request.subscribe({
      next: (updated) => { this.products.update((items) => items.map((item) => item.id === updated.id ? updated : item)); this.message.set(this.operation === 'sale' ? 'Venda registrada com sucesso.' : 'Reposicao registrada com sucesso.'); this.quantity = 1; this.processing.set(false); },
      error: (response) => this.showError(response, 'Nao foi possivel atualizar o estoque.')
    });
  }

  private showError(response: { error?: { message?: string; details?: string } }, fallback: string): void { this.processing.set(false); this.error.set(response.error?.message || response.error?.details || fallback); }
}
