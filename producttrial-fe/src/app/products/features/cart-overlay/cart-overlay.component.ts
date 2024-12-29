import {Component, inject, OnInit} from '@angular/core';
import {ProductsService} from "../../data-access/products.service";
import {CurrencyPipe, NgForOf, NgIf} from "@angular/common";
import {Button} from "primeng/button";

@Component({
  selector: 'app-cart-overlay',
  standalone: true,
  imports: [CurrencyPipe, NgForOf, Button, NgIf],
  templateUrl: './cart-overlay.component.html',
  styleUrl: './cart-overlay.component.css'
})
export class CartOverlayComponent implements OnInit {
  private readonly productsService = inject(ProductsService);

  public readonly cartItems = this.productsService.cart;

  public readonly totalItemsCount = this.productsService.totalItemsCount;

  ngOnInit(): void {
    this.productsService.getCart().subscribe();
  }

  removeFromCart(id: string) {
    this.productsService.removeFromCart(id).subscribe();

  }

  getTotalPrice(): number {
    return this.cartItems().reduce((acc, item) => acc + item.product.price * item.quantity, 0);
  }

}
