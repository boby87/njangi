import { Component, input, computed, ChangeDetectionStrategy } from '@angular/core';
import { RoleMembre } from '../../models/membre.model';

@Component({
  selector: 'app-role-badge',
  standalone: true,
  templateUrl: './role-badge.component.html',
  styleUrl: './role-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleBadgeComponent {
  readonly role = input.required<RoleMembre>();
  readonly size = input<'sm' | 'md'>('md');

  readonly label = computed(() => {
    switch (this.role()) {
      case 'PRESIDENT': return 'Président';
      case 'TRESORIER': return 'Trésorier';
      case 'SECRETAIRE': return 'Secrétaire';
      case 'CREATEUR': return 'Créateur';
      case 'AUDITEUR': return 'Auditeur';
      case 'MEMBRE': return 'Membre';
      default: return this.role();
    }
  });

  readonly styleClasses = computed(() => {
    switch (this.role()) {
      case 'PRESIDENT':
        return 'bg-indigo-500/15 text-indigo-300 border-indigo-500/30 shadow-sm shadow-indigo-500/10';
      case 'TRESORIER':
        return 'bg-amber-500/15 text-amber-300 border-amber-500/30 shadow-sm shadow-amber-500/10';
      case 'SECRETAIRE':
        return 'bg-sky-500/15 text-sky-300 border-sky-500/30 shadow-sm shadow-sky-500/10';
      case 'CREATEUR':
        return 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30 shadow-sm shadow-emerald-500/10';
      case 'AUDITEUR':
        return 'bg-cyan-500/15 text-cyan-300 border-cyan-500/30 shadow-sm shadow-cyan-500/10';
      case 'MEMBRE':
      default:
        return 'bg-emerald-500/10 text-emerald-300 border-emerald-500/25';
    }
  });
}
