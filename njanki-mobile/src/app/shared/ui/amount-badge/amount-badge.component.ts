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
  readonly variant = input<'primary' | 'success' | 'warning' | 'danger' | 'neutral' | 'plain'>('neutral');
  readonly size = input<'sm' | 'md' | 'lg' | 'xl' | 'hero'>('md');

  readonly formattedNumber = computed(() => {
    const val = Math.round(this.amount() || 0);
    return val.toLocaleString('fr-FR');
  });

  readonly formattedAmount = computed(() => {
    return `${this.formattedNumber()} XAF`;
  });
}
