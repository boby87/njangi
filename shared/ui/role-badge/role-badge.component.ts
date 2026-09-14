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
        return 'bg-purple-950/60 text-purple-300 border-purple-700/50';
      case 'TRESORIER':
        return 'bg-amber-950/60 text-amber-300 border-amber-700/50';
      case 'SECRETAIRE':
        return 'bg-blue-950/60 text-blue-300 border-blue-700/50';
      case 'CREATEUR':
        return 'bg-emerald-950/60 text-emerald-300 border-emerald-700/50';
      case 'AUDITEUR':
        return 'bg-cyan-950/60 text-cyan-300 border-cyan-700/50';
      case 'MEMBRE':
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  });
}
