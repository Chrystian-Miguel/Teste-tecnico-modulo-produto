export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
}

export interface ProductForm {
  name: string;
  description: string;
  price: number | null;
  quantity: number | null;
}
