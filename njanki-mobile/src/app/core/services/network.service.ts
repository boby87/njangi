import { Injectable, signal, computed } from '@angular/core';
import { Network, ConnectionStatus } from '@capacitor/network';
import { from } from 'rxjs';

export type NetworkQuality = 'ONLINE' | 'DEGRADED_3G' | 'OFFLINE';

@Injectable({
  providedIn: 'root',
})
export class NetworkService {
  readonly isOnline = signal<boolean>(true);
  readonly connectionType = signal<string>('cellular');

  readonly is3GOrDegraded = computed(() => {
    return this.isOnline() && (this.connectionType() === 'cellular' || this.connectionType() === 'unknown');
  });

  readonly networkQuality = computed<NetworkQuality>(() => {
    if (!this.isOnline()) return 'OFFLINE';
    if (this.is3GOrDegraded()) return 'DEGRADED_3G';
    return 'ONLINE';
  });

  readonly networkStatusLabel = computed<string>(() => {
    switch (this.networkQuality()) {
      case 'OFFLINE':
        return 'Hors Ligne (Mode Local Actif)';
      case 'DEGRADED_3G':
        return 'Réseau 3G Instable (Transactions mises en file)';
      case 'ONLINE':
        return 'Connecté (Synchronisation en temps réel)';
    }
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
        this.isOnline.set(true);
      }
    });

    // Écouteur d'événements réseau Capacitor (Callback natif)
    Network.addListener('networkStatusChange', (status: ConnectionStatus) => {
      this.updateStatus(status);
    });
  }

  private updateStatus(status: ConnectionStatus): void {
    this.isOnline.set(status.connected);
    this.connectionType.set(status.connectionType);
  }

  /**
   * Permet de forcer un rafraîchissement manuel du statut réseau
   */
  checkConnection(): void {
    from(Network.getStatus()).subscribe({
      next: (status: ConnectionStatus) => this.updateStatus(status),
      error: () => this.isOnline.set(false)
    });
  }
}
