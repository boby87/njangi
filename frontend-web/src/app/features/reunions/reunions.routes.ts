import { Routes } from '@angular/router';
export const REUNIONS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./liste-reunions/liste-reunions.component').then(m => m.ListeReunionsComponent) }
];
