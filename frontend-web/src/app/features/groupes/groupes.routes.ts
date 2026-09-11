import { Routes } from '@angular/router';

export const GROUPES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./liste-groupes/liste-groupes.component').then(m => m.ListeGroupesComponent)
  }
];
