import { Routes } from '@angular/router';
export const COTISATIONS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./liste-cotisations/liste-cotisations.component').then(m => m.ListeCotisationsComponent) }
];
