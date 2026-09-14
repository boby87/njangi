import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'groupes',
        loadComponent: () => import('./features/groupes/groupe-list/groupe-list.component').then(m => m.GroupeListComponent)
      },
      {
        path: 'groupes/creer',
        loadComponent: () => import('./features/groupes/groupe-create/groupe-create.component').then(m => m.GroupeCreateComponent)
      },
      {
        path: 'groupes/:id',
        loadComponent: () => import('./features/groupes/groupe-detail/groupe-detail.component').then(m => m.GroupeDetailComponent)
      },
      {
        path: 'reunions',
        loadComponent: () => import('./features/reunions/reunion-live/reunion-live.component').then(m => m.ReunionLiveComponent)
      },
      {
        path: 'reunions/compte-rendu',
        loadComponent: () => import('./features/reunions/compte-rendu/compte-rendu.component').then(m => m.CompteRenduComponent)
      },
      {
        path: 'cotisations',
        loadComponent: () => import('./features/cotisations/cotisation-list/cotisation-list.component').then(m => m.CotisationListComponent)
      },
      {
        path: 'cotisations/pot',
        loadComponent: () => import('./features/cotisations/pot-tour/pot-tour.component').then(m => m.PotTourComponent)
      },
      {
        path: 'paiements',
        loadComponent: () => import('./features/paiements/paiement-form/paiement-form.component').then(m => m.PaiementFormComponent)
      },
      {
        path: 'penalites',
        loadComponent: () => import('./features/penalites/penalite-list/penalite-list.component').then(m => m.PenaliteListComponent)
      },
      {
        path: 'statistiques',
        loadComponent: () => import('./features/statistiques/statistiques.component').then(m => m.StatistiquesComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
