import { Component, input, computed, ChangeDetectionStrategy } from '@angular/core';

export type BadgeStatusType = 
  | 'PAYE' 
  | 'EN_ATTENTE' 
  | 'EN_RETARD' 
  | 'PENALISE' 
  | 'PRESENT' 
  | 'RETARD' 
  | 'ABSENT_JUSTIFIE' 
  | 'ABSENT_NON_JUSTIFIE'
  | 'ACTIF'
  | 'PLANIFIEE'
  | 'EN_COURS'
  | 'CLOTUREE'
  | 'A_VENIR'
  | 'VERSE';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusBadgeComponent {
  readonly status = input.required<string>();

  readonly label = computed(() => {
    switch (this.status()) {
      case 'PAYE': return 'Payé';
      case 'EN_ATTENTE': return 'En attente';
      case 'EN_RETARD': return 'En retard';
      case 'PENALISE': return 'Pénalisé';
      case 'PRESENT': return 'Présent';
      case 'RETARD': return 'En retard';
      case 'ABSENT_JUSTIFIE': return 'Abs. justifiée';
      case 'ABSENT_NON_JUSTIFIE': return 'Abs. non justifiée';
      case 'ACTIF': return 'Actif';
      case 'PLANIFIEE': return 'Planifiée';
      case 'EN_COURS': return 'En cours';
      case 'CLOTUREE': return 'Clôturée';
      case 'A_VENIR': return 'À venir';
      case 'VERSE': return 'Versé';
      default: return this.status();
    }
  });

  readonly styleClasses = computed(() => {
    switch (this.status()) {
      case 'PAYE':
      case 'PRESENT':
      case 'ACTIF':
      case 'VERSE':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'EN_ATTENTE':
      case 'A_VENIR':
      case 'PLANIFIEE':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'EN_COURS':
        return 'bg-sky-500/10 text-sky-400 border-sky-500/30 animate-pulse';
      case 'EN_RETARD':
      case 'RETARD':
      case 'ABSENT_JUSTIFIE':
        return 'bg-orange-500/10 text-orange-400 border-orange-500/30';
      case 'PENALISE':
      case 'ABSENT_NON_JUSTIFIE':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      case 'CLOTUREE':
        return 'bg-slate-500/10 text-slate-400 border-slate-500/30';
      default:
        return 'bg-slate-500/10 text-slate-300 border-slate-700';
    }
  });
}
