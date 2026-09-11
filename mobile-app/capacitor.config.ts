import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.njangi.mobile',
  appName: 'Njangi',
  webDir: 'www',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    StatusBar: {
      style: 'Default',
      backgroundColor: '#1a237e'
    }
  }
};

export default config;
