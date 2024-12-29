import {Product} from "./product.model";

export interface CartItem {
  id: string;
  product: Product;
  quantity: number;
}
