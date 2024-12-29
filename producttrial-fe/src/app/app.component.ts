import {
  Component, inject, OnInit,
} from "@angular/core";
import {RouterModule} from "@angular/router";
import {SplitterModule} from 'primeng/splitter';
import {ToolbarModule} from 'primeng/toolbar';
import {PanelMenuComponent} from "./shared/ui/panel-menu/panel-menu.component";
import {BadgeModule} from "primeng/badge";
import {OverlayPanelModule} from "primeng/overlaypanel";
import {CartOverlayComponent} from "./products/features/cart-overlay/cart-overlay.component";
import {ProductsService} from "./products/data-access/products.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
  standalone: true,
  imports: [RouterModule, SplitterModule, ToolbarModule, PanelMenuComponent, BadgeModule, OverlayPanelModule, CartOverlayComponent],
})
export class AppComponent {
  title = "ALTEN SHOP";

  private readonly productsService = inject(ProductsService);

  public readonly totalItemsCount = this.productsService.totalItemsCount;
  
}
