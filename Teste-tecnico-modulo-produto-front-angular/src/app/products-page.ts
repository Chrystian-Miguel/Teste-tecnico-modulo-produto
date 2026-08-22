import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Product, ProductForm } from './product.model';

@Component({
  selector: 'app-products-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './products-page.html',
  styleUrl: './products-page.scss'
})
export class ProductsPage implements OnInit {
  private readonly apiUrl = '/api/v1/products';
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected searchTerm = '';
  protected currentPage = 1;
  protected readonly pageSize = 6;
  protected editingProduct: Product | null = null;
  protected form: ProductForm = this.emptyForm();

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void { this.loadProducts(); }

  protected get filteredProducts(): Product[] {
    const term = this.searchTerm.trim().toLowerCase();
    return this.products().filter((product) => product.name.toLowerCase().includes(term));
  }

  protected get pagedProducts(): Product[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredProducts.slice(start, start + this.pageSize);
  }

  protected get pageCount(): number { return Math.max(1, Math.ceil(this.filteredProducts.length / this.pageSize)); }
  protected get totalStock(): number { return this.products().reduce((total, product) => total + product.quantity, 0); }
  protected get lowStockCount(): number { return this.products().filter((product) => product.quantity <= 5).length; }

  protected updateSearch(): void { this.currentPage = 1; }
  protected goToPage(page: number): void { this.currentPage = Math.min(Math.max(page, 1), this.pageCount); }
  protected pageNumbers(): number[] { return Array.from({ length: this.pageCount }, (_, index) => index + 1); }
  protected openCreate(): void { this.editingProduct = null; this.form = this.emptyForm(); this.clearFeedback(); }

  protected openEdit(product: Product): void {
    this.editingProduct = product;
    this.form = { name: product.name, description: product.description, price: product.price, quantity: product.quantity };
    this.clearFeedback();
  }

  protected cancelEdit(): void { this.editingProduct = null; this.form = this.emptyForm(); }

  protected saveProduct(): void {
    if (!this.form.name.trim() || !this.form.description.trim() || !this.form.price || this.form.price <= 0 || this.form.quantity === null || this.form.quantity < 0) {
      this.error.set('Preencha os campos com valores validos.');
      return;
    }
    this.saving.set(true);
    this.clearFeedback();
    const request = this.editingProduct
      ? this.http.put<Product>(`${this.apiUrl}/${this.editingProduct.id}`, this.form)
      : this.http.post<Product>(this.apiUrl, this.form);
    request.subscribe({
      next: (product) => {
        this.products.update((items) => this.editingProduct ? items.map((item) => item.id === product.id ? product : item) : [product, ...items]);
        this.message.set(this.editingProduct ? 'Produto atualizado.' : 'Produto cadastrado.');
        this.cancelEdit();
        this.saving.set(false);
      },
      error: (response) => this.handleError(response, 'Nao foi possivel salvar o produto.')
    });
  }

  protected deleteProduct(product: Product): void {
    if (!window.confirm(`Excluir ${product.name}?`)) return;
    this.http.delete(`${this.apiUrl}/${product.id}`).subscribe({
      next: () => { this.products.update((items) => items.filter((item) => item.id !== product.id)); this.message.set('Produto removido.'); this.goToPage(this.currentPage); },
      error: (response) => this.handleError(response, 'Nao foi possivel remover o produto.')
    });
  }

  private loadProducts(): void {
    this.loading.set(true);
    this.http.get<Product[]>(this.apiUrl).subscribe({
      next: (products) => { this.products.set(products); this.loading.set(false); },
      error: (response) => { this.handleError(response, 'Nao foi possivel carregar os produtos.'); this.loading.set(false); }
    });
  }

  private handleError(response: { error?: { message?: string; details?: string } }, fallback: string): void {
    this.saving.set(false);
    this.error.set(response.error?.message || response.error?.details || fallback);
  }

  private clearFeedback(): void { this.message.set(''); this.error.set(''); }
  private emptyForm(): ProductForm { return { name: '', description: '', price: null, quantity: 0 }; }
}
