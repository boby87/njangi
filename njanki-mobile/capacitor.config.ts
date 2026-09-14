import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'cm.njangi.app',
  appName: 'Njangi',
  webDir: 'www',
  server: {
    cleartext: true,
    androidScheme: 'http'
  },
  android: {
    allowMixedContent: true,
    captureInput: true
  }
};

export default config;
