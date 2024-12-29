import {Injectable, inject, signal} from "@angular/core";
import {Product} from "./product.model";
import {HttpClient} from "@angular/common/http";
import {catchError, Observable, of, tap} from "rxjs";
import {CartItem} from "./cart-item.model";
import {v4 as uuidv4} from "uuid";

@Injectable({
  providedIn: "root"
})
export class ProductsService {

  private readonly http = inject(HttpClient);
  private readonly productsApiUrl = "/api/products";
  private readonly cartApiUrl = "/api/cart";

  private readonly _products = signal<Product[]>([]);
  private readonly _cart = signal<CartItem[]>([]);
  private readonly _totalItemsCount = signal<number>(0);

  public readonly products = this._products.asReadonly();
  public readonly cart = this._cart.asReadonly();
  public readonly totalItemsCount = this._totalItemsCount.asReadonly();


  public get(): Observable<Product[]> {
    return this.http.get<Product[]>(this.productsApiUrl).pipe(
      catchError((error) => {
        return this.http.get<Product[]>("assets/products.json");
      }),
      tap((products) => this._products.set(products)),
    );
  }

  public create(product: Product): Observable<boolean> {
    return this.http.post<boolean>(this.productsApiUrl, product).pipe(
      catchError(() => {
        return of(true);
      }),
      tap(() => this._products.update(products => [product, ...products])),
    );
  }

  public update(product: Product): Observable<boolean> {
    return this.http.patch<boolean>(`${this.productsApiUrl}/${product.id}`, product).pipe(
      catchError(() => {
        return of(true);
      }),
      tap(() => this._products.update(products => {
        return products.map(p => p.id === product.id ? product : p)
      })),
    );
  }

  public delete(productId: number): Observable<boolean> {
    return this.http.delete<boolean>(`${this.productsApiUrl}/${productId}`).pipe(
      catchError(() => {
        return of(true);
      }),
      tap(() => this._products.update(products => products.filter(product => product.id !== productId))),
    );
  }


  public addToCart(product: Product): Observable<boolean> {
    const cartItemId = uuidv4();
    const existingItem = this._cart().find(item => item.product.id === product.id);

    if (existingItem) {
      existingItem.quantity += 1;
      return this.updateCartItem(existingItem);
    } else {
      const newCartItem: CartItem = {id: cartItemId, product, quantity: 1};
      return this.addNewCartItem(newCartItem);
    }
  }

  private addNewCartItem(cartItem: CartItem): Observable<boolean> {
    return this.http.post<boolean>(this.cartApiUrl, cartItem).pipe(
      catchError(() => of(false)),
      tap(() => {
        this._cart.update((cart) => [...cart, cartItem]);
        this.updateTotalItemsCount();
      })
    );
  }


  private updateCartItem(cartItem: CartItem): Observable<boolean> {
    return this.http.put<boolean>(`${this.cartApiUrl}/${cartItem.id}`, cartItem).pipe(
      catchError(() => of(false)),
      tap(() => {
        this._cart.update((cart) =>
          cart.map((item) => (item.id === cartItem.id ? cartItem : item))
        );
        this.updateTotalItemsCount();
      })
    );
  }

  public removeFromCart(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.cartApiUrl}/${id}`).pipe(
      catchError(() => of(false)),
      tap(() => {
        this._cart.update((cart) => cart.filter((item) => item.id !== id));
        this.updateTotalItemsCount();
      })
    );
  }

  public getCart(): Observable<CartItem[]> {
    return this.http.get<CartItem[]>(this.cartApiUrl).pipe(
      catchError(() => of([])),
      tap((cartItems) => {
        this._cart.set(cartItems);
        this.updateTotalItemsCount();
      }),
    );
  }

  private updateTotalItemsCount(): void {
    const total = this._cart().reduce((acc, item) => acc + item.quantity, 0);
    this._totalItemsCount.set(total);
  }
}
