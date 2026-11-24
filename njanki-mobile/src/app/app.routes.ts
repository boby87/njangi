import { Routes } from '@angular/router';
import { from } from 'rxjs';
import { map } from 'rxjs/operators';
import { mobileAuthGuard, mobileGuestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [mobileGuestGuard],
    loadComponent: () => from(import('./pages/login/login.page')).pipe(map((m) => m.LoginPage)),
  },
  {
    path: 'register',
    canActivate: [mobileGuestGuard],
    loadComponent: () => from(import('./pages/register/register.page')).pipe(map((m) => m.RegisterPage)),
  },
  {
    path: 'creer-groupe',
    canActivate: [mobileAuthGuard],
    loadComponent: () => from(import('./pages/creer-groupe/creer-groupe.page')).pipe(map((m) => m.CreerGroupePage)),
  },
  {
    path: 'tabs',
    canActivate: [mobileAuthGuard],
    loadComponent: () => from(import('./layout/tabs/tabs.page')).pipe(map((m) => m.TabsPage)),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => from(import('./pages/dashboard/dashboard.page')).pipe(map((m) => m.DashboardPage)),
      },
      {
        path: 'cotisations',
        loadComponent: () => from(import('./pages/cotisations/cotisations.page')).pipe(map((m) => m.CotisationsPage)),
      },
      {
        path: 'reunions',
        loadComponent: () => from(import('./pages/reunions/reunions.page')).pipe(map((m) => m.ReunionsPage)),
      },
      {
        path: 'paiements',
        loadComponent: () => from(import('./pages/paiements/paiements.page')).pipe(map((m) => m.PaiementsPage)),
      },
      {
        path: 'profil',
        loadComponent: () => from(import('./pages/profil/profil.page')).pipe(map((m) => m.ProfilPage)),
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '',
    redirectTo: 'tabs/dashboard',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'tabs/dashboard',
  },
];
