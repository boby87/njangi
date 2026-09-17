// scripts/record-demo.js
// Automatisation et enregistrement vidéo de la démonstration réelle Njangi
const path = require('path');
const fs = require('fs');
const pw = require('C:/Users/fokou/AppData/Local/ms-playwright-go/1.57.0/package');

const ARTIFACT_DIR = 'C:/Users/fokou/.gemini/antigravity-ide/brain/1b4455f3-5eca-42cb-8e33-7e5ce2a0c2aa';
const CHROME_PATH = 'C:/Users/fokou/AppData/Local/ms-playwright/chromium-1155/chrome-win/chrome.exe';

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function run() {
  console.log('[1/7] Lancement du navigateur Chromium...');
  const browser = await pw.chromium.launch({
    headless: true,
    executablePath: CHROME_PATH,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  console.log('[2/7] Initialisation du contexte vidéo (1280x800)...');
  const context = await browser.newContext({
    viewport: { width: 1280, height: 800 },
    recordVideo: {
      dir: ARTIFACT_DIR,
      size: { width: 1280, height: 800 }
    }
  });

  const page = await context.newPage();

  console.log('[3/7] Navigation vers le Dashboard (http://localhost:4000/dashboard)...');
  await page.goto('http://localhost:4000/dashboard', { waitUntil: 'networkidle', timeout: 30000 });
  await sleep(3000);

  // Démonstration interactive du changement de rôles
  console.log('[4/7] Démonstration dynamique des 5 rôles métier...');
  
  // Rôle Président
  const btnPresident = page.locator('button:has-text("Président"), button:has-text("President")').first();
  if (await btnPresident.count() > 0) {
    await btnPresident.click();
    console.log('  -> Vue Président activée');
    await sleep(3000);
  }

  // Rôle Trésorier
  const btnTresorier = page.locator('button:has-text("Trésorier"), button:has-text("Tresorier")').first();
  if (await btnTresorier.count() > 0) {
    await btnTresorier.click();
    console.log('  -> Vue Trésorier activée');
    await sleep(3000);
  }

  // Rôle Secrétaire
  const btnSecretaire = page.locator('button:has-text("Secrétaire"), button:has-text("Secretaire")').first();
  if (await btnSecretaire.count() > 0) {
    await btnSecretaire.click();
    console.log('  -> Vue Secrétaire activée');
    await sleep(3000);
  }

  // Rôle Membre
  const btnMembre = page.locator('button:has-text("Membre")').first();
  if (await btnMembre.count() > 0) {
    await btnMembre.click();
    console.log('  -> Vue Membre activée');
    await sleep(3000);
  }

  // Rôle Créateur
  const btnCreateur = page.locator('button:has-text("Créateur"), button:has-text("Createur")').first();
  if (await btnCreateur.count() > 0) {
    await btnCreateur.click();
    console.log('  -> Vue Créateur activée');
    await sleep(3000);
  }

  // Remettre sur Président
  if (await btnPresident.count() > 0) {
    await btnPresident.click();
    await sleep(1500);
  }

  // Module Groupes
  console.log('[5/7] Navigation vers /groupes et /groupes/creer...');
  await page.goto('http://localhost:4000/groupes', { waitUntil: 'networkidle' });
  await sleep(3000);

  await page.goto('http://localhost:4000/groupes/creer', { waitUntil: 'networkidle' });
  await sleep(2000);
  const nomInput = page.locator('input[type="text"]').first();
  if (await nomInput.count() > 0) {
    await nomInput.fill('Tontine Solidarite Cameroun');
  }
  await sleep(2000);

  // Module Séances Live
  console.log('[6/7] Navigation vers /reunions et /paiements...');
  await page.goto('http://localhost:4000/reunions', { waitUntil: 'networkidle' });
  await sleep(3500);

  // Module Caisse & Paiements
  await page.goto('http://localhost:4000/paiements', { waitUntil: 'networkidle' });
  await sleep(3500);

  // Module Statistiques
  console.log('[7/7] Navigation vers /statistiques (Bilan de Trésorerie)...');
  await page.goto('http://localhost:4000/statistiques', { waitUntil: 'networkidle' });
  await sleep(3500);

  // Retour final au Dashboard
  await page.goto('http://localhost:4000/dashboard', { waitUntil: 'networkidle' });
  await sleep(2500);

  console.log('Finalisation de l\'enregistrement vidéo...');
  await page.close();
  const videoPath = await page.video().path();
  await context.close();
  await browser.close();

  const finalVideoPath = path.join(ARTIFACT_DIR, 'njangi_demo_real.webm');
  if (fs.existsSync(videoPath)) {
    fs.copyFileSync(videoPath, finalVideoPath);
    console.log(`[SUCCÈS] Vidéo enregistrée avec succès : ${finalVideoPath}`);
  } else {
    console.log(`[ATTENTION] Vidéo générée à : ${videoPath}`);
  }
}

run().catch(err => {
  console.error('Erreur lors de l\'enregistrement :', err);
  process.exit(1);
});
