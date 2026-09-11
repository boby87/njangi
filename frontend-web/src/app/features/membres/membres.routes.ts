import { Routes } from '@angular/router';
export const MEMBRES_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./liste-membres/liste-membres.component').then(m => m.ListeMembresComponent) }
];
