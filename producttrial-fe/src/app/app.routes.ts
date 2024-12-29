import {Routes} from "@angular/router";
import {HomeComponent} from "./shared/features/home/home.component";

export const APP_ROUTES: Routes = [
  {
    path: "home",
    component: HomeComponent,
  },
  {
    path: "products",
    loadChildren: () =>
      import("./products/products.routes").then((m) => m.PRODUCTS_ROUTES)
  },
  {
    path: "contact",
    loadChildren: () =>
      import("./products/contact.routes").then((m) => m.CONTACT_ROUTES)
  },
  {path: "", redirectTo: "home", pathMatch: "full"},
];
