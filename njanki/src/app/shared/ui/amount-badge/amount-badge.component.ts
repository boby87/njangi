import { Component, input, computed, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-amount-badge',
  standalone: true,
  templateUrl: './amount-badge.component.html',
  styleUrl: './amount-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AmountBadgeComponent {
  readonly amount = input.required<number>();
  readonly variant = input<'primary' | 'success' | 'warning' | 'danger' | 'neutral'>('neutral');
  readonly size = input<'sm' | 'md' | 'lg'>('md');

  // Formatage monétaire strict XAF sans décimales (ex. 10 000 XAF)
  readonly formattedAmount = computed(() => {
    const val = Math.round(this.amount() || 0);
    return `${val.toLocaleString('fr-FR')} XAF`;
  });
}
