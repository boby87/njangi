import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ApiResponse } from '@shared/models/api-response.model';
import { Membre } from '@shared/models/membre.model';
import { MobileApiService } from './api.service';
import { MobileAuthService } from './auth.service';
import { NetworkService } from './network.service';
import {
  CACHED_STATE_KEY,
  MobileTontineStateService,
  OFFLINE_QUEUE_KEY,
  OfflineAction,
} from './tontine-state.service';

const prefsStore = vi.hoisted(() => new Map<string, string>());
const preferencesMocks = vi.hoisted(() => ({
  get: vi.fn(({ key }: { key: string }) => [{ value: prefsStore.get(key) ?? null }]),
  set: vi.fn(({ key, value }: { key: string; value: string }) => {
    prefsStore.set(key, value);
    return [undefined];
  }),
  remove: vi.fn(({ key }: { key: string }) => {
    prefsStore.delete(key);
    return [undefined];
  }),
}));

vi.mock('@capacitor/preferences', () => ({
  Preferences: preferencesMocks,
}));

describe('MobileTontineStateService (File hors-ligne persistée + Synchronisation Gateway)', () => {
  let service: MobileTontineStateService;
  let isOnlineSignal = signal<boolean>(false);
  let isAuthenticatedSignal = signal<boolean>(true);
  let currentUserSignal = signal<Membre | null>(null);

  let apiPostSpy: ReturnType<typeof vi.fn>;
  let apiGetSpy: ReturnType<typeof vi.fn>;

  const demoUser: Membre = {
    id: 'm-001',
    authUtilisateurId: 'u-001',
    nom: 'FOKOU',
    prenom: 'Jean-Paul',
    email: 'jp.fokou@njangi.cm',
    telephone: '+237699123456',
    ville: 'Yaoundé',
    pays: 'Cameroun',
    statut: 'ACTIF',
    dateInscription: '2025-01-15',
    estVerifieKyc: true,
  };

  function okResponse<T>(data: T): ApiResponse<T> {
    return { success: true, message: 'OK', data, errorCode: null };
  }

  function failResponse<T>(message = 'Erreur serveur'): ApiResponse<T> {
    return { success: false, message, data: null as unknown as T, errorCode: 'HTTP_503' };
  }

  function setupTestBed(initialPrefs?: Record<string, string>): void {
    prefsStore.clear();
    if (initialPrefs) {
      Object.entries(initialPrefs).forEach(([k, v]) => prefsStore.set(k, v));
    }
    preferencesMocks.get.mockClear();
    preferencesMocks.set.mockClear();
    preferencesMocks.remove.mockClear();

    isOnlineSignal = signal<boolean>(false);
    isAuthenticatedSignal = signal<boolean>(true);
    currentUserSignal = signal<Membre | null>(demoUser);

    apiPostSpy = vi.fn(() => of(okResponse({ id: 'srv-1' })));
    apiGetSpy = vi.fn(() => of(okResponse([])));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: MobileApiService,
          useValue: {
            get: apiGetSpy,
            post: apiPostSpy,
            put: vi.fn(() => of(okResponse({}))),
            delete: vi.fn(() => of(okResponse({}))),
          },
        },
        {
          provide: MobileAuthService,
          useValue: {
            isAuthenticated: isAuthenticatedSignal,
            currentUser: currentUserSignal,
          },
        },
        {
          provide: NetworkService,
          useValue: {
            isOnline: isOnlineSignal,
            connectionType: signal<string>('none'),
          },
        },
      ],
    });

    service = TestBed.inject(MobileTontineStateService);
    TestBed.tick();
  }

  it('devrait mettre en file hors-ligne et persister dans @capacitor/preferences lors des mutations offline', () => {
    setupTestBed();

    service.marquerPresence('m-001', 'PRESENT');
    service.enregistrerPaiement({
      cotisationId: 'cot-001',
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      membreId: 'm-001',
      membreNom: 'Jean-Paul FOKOU',
      montantXaf: 50000,
      modePaiement: 'CASH',
      typeCotisation: 'ROTATIVE_POT',
      preuveUrl: 'https://example.com/recu.jpg',
    });
    service.infligerSanction({
      groupeId: 'grp-001',
      sessionId: 'ses-2026',
      reunionId: 'reu-live-03',
      membreId: 'm-004',
      typeInfraction: 'RETARD_REUNION',
      montantXaf: 1000,
      motif: 'Retard de 20 min',
    });

    expect(service.offlineQueue().length).toBe(3);
    expect(service.offlineQueue().map((a) => a.type)).toEqual([
      'PRESENCE',
      'CASH_PAYMENT',
      'SANCTION',
    ]);
    expect(apiPostSpy).not.toHaveBeenCalled();
    expect(preferencesMocks.set).toHaveBeenCalledWith(
      expect.objectContaining({ key: OFFLINE_QUEUE_KEY })
    );

    const persistedRaw = prefsStore.get(OFFLINE_QUEUE_KEY);
    expect(persistedRaw).toBeTruthy();
    const persistedQueue = JSON.parse(persistedRaw ?? '[]') as OfflineAction[];
    expect(persistedQueue.length).toBe(3);
    expect(persistedQueue[1].payload['cleIdempotence']).toBeTruthy();

    const cachedRaw = prefsStore.get(CACHED_STATE_KEY);
    expect(cachedRaw).toBeTruthy();
  });

  it('devrait restaurer la file hors-ligne persistée depuis @capacitor/preferences au démarrage', () => {
    const savedQueue: OfflineAction[] = [
      {
        id: 'offline-restored-1',
        type: 'CREATE_GROUP',
        payload: { nom: 'Njangi Restored', typeSiege: 'ROTATIF' },
        timestamp: '2026-03-07T10:00:00Z',
      },
    ];

    setupTestBed({
      [OFFLINE_QUEUE_KEY]: JSON.stringify(savedQueue),
    });

    expect(preferencesMocks.get).toHaveBeenCalledWith({ key: OFFLINE_QUEUE_KEY });
    expect(service.offlineQueue().length).toBe(1);
    expect(service.offlineQueue()[0].id).toBe('offline-restored-1');
    expect(service.offlineQueue()[0].type).toBe('CREATE_GROUP');
  });

  it('devrait rejouer les actions hors-ligne vers la Gateway, retirer les succès et conserver les échecs', () => {
    const queuedActions: OfflineAction[] = [
      {
        id: 'act-1',
        type: 'PRESENCE',
        payload: {
          reunionId: 'reu-99',
          membreId: 'm-001',
          statut: 'PRESENT',
          heureArrivee: '2026-03-07T14:00:00Z',
        },
        timestamp: '2026-03-07T14:00:00Z',
      },
      {
        id: 'act-2',
        type: 'CASH_PAYMENT',
        payload: {
          cotisationId: 'cot-1',
          membreId: 'm-001',
          groupeId: 'grp-001',
          montant: 50000,
          cleIdempotence: 'idemp-fail',
          pieceJointeUrl: 'recu.jpg',
        },
        timestamp: '2026-03-07T14:01:00Z',
      },
      {
        id: 'act-3',
        type: 'MOMO_PAYMENT',
        payload: {
          cotisationId: 'cot-2',
          membreId: 'm-002',
          groupeId: 'grp-001',
          montant: 5000,
          cleIdempotence: 'idemp-ok',
          numeroTelephone: '+237677123456',
          modePaiement: 'MTN_MOMO',
        },
        timestamp: '2026-03-07T14:02:00Z',
      },
      {
        id: 'act-4',
        type: 'SANCTION',
        payload: {
          membreId: 'm-004',
          groupeId: 'grp-001',
          sessionId: 'ses-2026',
          reunionId: 'reu-99',
          typeInfraction: 'RETARD_REUNION',
          montant: 1000,
          motif: 'Retard',
        },
        timestamp: '2026-03-07T14:03:00Z',
      },
    ];

    setupTestBed({
      [OFFLINE_QUEUE_KEY]: JSON.stringify(queuedActions),
    });

    apiPostSpy.mockImplementation((endpoint: string) => {
      if (endpoint === '/api/v1/paiements/cash') {
        return of(failResponse('Passerelle paiements cash temporairement indisponible'));
      }
      return of(okResponse({ synced: true }));
    });

    isOnlineSignal.set(true);
    TestBed.tick();

    expect(apiPostSpy).toHaveBeenCalledWith(
      '/api/v1/reunions/reu-99/presences',
      expect.objectContaining({ membreId: 'm-001', statut: 'PRESENT' })
    );
    expect(apiPostSpy).toHaveBeenCalledWith(
      '/api/v1/paiements/cash',
      expect.objectContaining({ cleIdempotence: 'idemp-fail' })
    );
    expect(apiPostSpy).toHaveBeenCalledWith(
      '/api/v1/paiements/mobile-money',
      expect.objectContaining({ cleIdempotence: 'idemp-ok', modePaiement: 'MTN_MOMO' })
    );
    expect(apiPostSpy).toHaveBeenCalledWith(
      '/api/v1/penalites',
      expect.objectContaining({ typeInfraction: 'RETARD_REUNION' })
    );

    expect(service.isSyncing()).toBe(false);
    expect(service.offlineQueue().length).toBe(1);
    expect(service.offlineQueue()[0].id).toBe('act-2');
    expect(service.offlineQueue()[0].retryCount).toBe(1);
    expect(service.syncError()).toContain('temporairement indisponible');

    const remainingPersisted = JSON.parse(
      prefsStore.get(OFFLINE_QUEUE_KEY) ?? '[]'
    ) as OfflineAction[];
    expect(remainingPersisted.length).toBe(1);
    expect(remainingPersisted[0].id).toBe('act-2');
  });

  it('devrait vider la file et mettre à jour lastSyncAt quand toutes les actions réussissent', () => {
    setupTestBed();
    service.creerGroupe({
      nom: 'Njangi Douala Bonapriso',
      description: 'Épargne mensuelle',
      typeSiege: 'ROTATIF',
      frequenceReunion: 'MENSUELLE',
      montantCotisationBaseXaf: 50000,
      nombreMembresMax: 12,
    });

    expect(service.offlineQueue().length).toBe(1);
    expect(service.offlineQueue()[0].type).toBe('CREATE_GROUP');

    isOnlineSignal.set(true);
    TestBed.tick();

    expect(apiPostSpy).toHaveBeenCalledWith(
      '/api/v1/groupes',
      expect.objectContaining({ nom: 'Njangi Douala Bonapriso', typeSiege: 'ROTATIF' })
    );
    expect(service.offlineQueue().length).toBe(0);
    expect(service.syncError()).toBeNull();
    expect(service.lastSyncAt()).toBeTruthy();
    expect(JSON.parse(prefsStore.get(OFFLINE_QUEUE_KEY) ?? '[]')).toEqual([]);
  });
});
