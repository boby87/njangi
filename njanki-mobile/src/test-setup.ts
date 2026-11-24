// Polyfills for running unit tests under jsdom (the default Vitest environment).
// Ionic components such as ion-menu and ion-split-pane query `window.matchMedia`,
// which jsdom does not implement.
if (!window.matchMedia) {
  window.matchMedia = (query: string): MediaQueryList =>
    ({
      matches: false,
      media: query,
      onchange: null,
      addListener: () => undefined,
      removeListener: () => undefined,
      addEventListener: () => undefined,
      removeEventListener: () => undefined,
      dispatchEvent: () => false,
    }) as MediaQueryList;
}

// Node 25+ exposes an incomplete global Storage stub unless --localstorage-file is set,
// which causes @capacitor/preferences web fallback to throw inside jsdom tests.
const storageStore = new Map<string, string>();
const memoryStorage: Storage = {
  get length(): number {
    return storageStore.size;
  },
  clear(): void {
    storageStore.clear();
  },
  getItem(key: string): string | null {
    return storageStore.has(key) ? (storageStore.get(key) ?? null) : null;
  },
  key(index: number): string | null {
    return Array.from(storageStore.keys())[index] ?? null;
  },
  removeItem(key: string): void {
    storageStore.delete(key);
  },
  setItem(key: string, value: string): void {
    storageStore.set(key, String(value));
  },
};

if (typeof window !== 'undefined' && typeof window.localStorage?.getItem !== 'function') {
  Object.defineProperty(window, 'localStorage', {
    value: memoryStorage,
    configurable: true,
    writable: true,
  });
}
