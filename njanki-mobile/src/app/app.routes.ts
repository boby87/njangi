import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.page').then((m) => m.RegisterPage),
  },
  {
    path: 'creer-groupe',
    loadComponent: () => import('./pages/creer-groupe/creer-groupe.page').then((m) => m.CreerGroupePage),
  },
  {
    path: 'tabs',
    loadComponent: () => import('./layout/tabs/tabs.page').then((m) => m.TabsPage),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.page').then((m) => m.DashboardPage),
      },
      {
        path: 'cotisations',
        loadComponent: () => import('./pages/cotisations/cotisations.page').then((m) => m.CotisationsPage),
      },
      {
        path: 'reunions',
        loadComponent: () => import('./pages/reunions/reunions.page').then((m) => m.ReunionsPage),
      },
      {
        path: 'paiements',
        loadComponent: () => import('./pages/paiements/paiements.page').then((m) => m.PaiementsPage),
      },
      {
        path: 'profil',
        loadComponent: () => import('./pages/profil/profil.page').then((m) => m.ProfilPage),
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
