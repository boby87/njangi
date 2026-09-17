import { Component, input, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-proof-preview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './proof-preview.component.html',
  styleUrl: './proof-preview.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProofPreviewComponent {
  readonly imageUrl = input<string | undefined>(undefined);
  readonly title = input<string>('Preuve de Paiement Cash (Reçu Signé)');
  readonly closeModal = output<void>();

  readonly isFullscreen = signal<boolean>(false);

  toggleFullscreen(): void {
    this.isFullscreen.update((val: boolean) => !val);
  }

  onClose(): void {
    this.closeModal.emit();
  }
}
