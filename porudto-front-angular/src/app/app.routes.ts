import { Routes } from '@angular/router';
import { ProductsPage } from './products-page';
import { OperationsPage } from './operations-page';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'produtos' },
	{ path: 'produtos', component: ProductsPage },
	{ path: 'movimentacoes', component: OperationsPage },
	{ path: '**', redirectTo: 'produtos' }
];
