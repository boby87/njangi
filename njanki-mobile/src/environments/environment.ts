export const environment = {
  production: false,
  // Sur émulateur Android, 10.0.2.2 redirige vers le localhost de la machine hôte (api-gateway:9090)
  // En test sur navigateur Web, basculer vers http://localhost:9090
  apiUrl: 'http://10.0.2.2:9090',
  gatewayWebUrl: 'http://localhost:9090'
};
