import { Injectable, signal, computed } from '@angular/core';
import { Network, ConnectionStatus } from '@capacitor/network';
import { from } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NetworkService {
  readonly isOnline = signal<boolean>(true);
  readonly connectionType = signal<string>('cellular');

  readonly is3GOrDegraded = computed(() => {
    return this.isOnline() && (this.connectionType() === 'cellular' || this.connectionType() === 'unknown');
  });

  constructor() {
    this.initNetworkMonitoring();
  }

  private initNetworkMonitoring(): void {
    // Lecture du statut initial via RxJS from() sans aucun async/await
    from(Network.getStatus()).subscribe({
      next: (status: ConnectionStatus) => {
        this.updateStatus(status);
      },
      error: () => {
        // En cas d'erreur de plugin (ex: navigateur sans mock), considérer connecté
        this.isOnline.set(true);
      }
    });

    // Écouteur d'événements réseau Capacitor
    Network.addListener('networkStatusChange', (status: ConnectionStatus) => {
      this.updateStatus(status);
    });
  }

  private updateStatus(status: ConnectionStatus): void {
    this.isOnline.set(status.connected);
    this.connectionType.set(status.connectionType);
  }
}
