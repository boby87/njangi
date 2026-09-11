import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/tabs/tableau-de-bord',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.page').then(m => m.LoginPage)
      }
    ]
  },
  {
    path: 'tabs',
    loadComponent: () => import('./features/tabs/tabs.page').then(m => m.TabsPage),
    canActivate: [authGuard],
    children: [
      {
        path: 'tableau-de-bord',
        loadComponent: () => import('./features/tableau-de-bord/tableau-de-bord.page').then(m => m.TableauDeBordPage)
      },
      {
        path: 'groupes',
        loadComponent: () => import('./features/groupes/liste-groupes/liste-groupes.page').then(m => m.ListeGroupesPage)
      },
      {
        path: 'reunions',
        loadComponent: () => import('./features/reunions/liste-reunions/liste-reunions.page').then(m => m.ListeReunionsPage)
      },
      {
        path: 'cotisations',
        loadComponent: () => import('./features/cotisations/liste-cotisations/liste-cotisations.page').then(m => m.ListeCotisationsPage)
      }
    ]
  }
];
