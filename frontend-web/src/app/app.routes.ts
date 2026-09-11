import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/tableau-de-bord',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'tableau-de-bord',
    loadComponent: () => import('./features/tableau-de-bord/tableau-de-bord.component').then(m => m.TableauDeBordComponent),
    canActivate: [authGuard]
  },
  {
    path: 'groupes',
    loadChildren: () => import('./features/groupes/groupes.routes').then(m => m.GROUPES_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'membres',
    loadChildren: () => import('./features/membres/membres.routes').then(m => m.MEMBRES_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'reunions',
    loadChildren: () => import('./features/reunions/reunions.routes').then(m => m.REUNIONS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: 'cotisations',
    loadChildren: () => import('./features/cotisations/cotisations.routes').then(m => m.COTISATIONS_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: '/tableau-de-bord'
  }
];
