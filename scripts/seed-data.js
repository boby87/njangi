// scripts/seed-data.js
const GATEWAY_URL = 'http://localhost:9090/api/v1';

async function api(path, method = 'GET', body = null, token = null) {
  const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const opts = { method, headers };
  if (body) opts.body = JSON.stringify(body);
  try {
    const res = await fetch(GATEWAY_URL + path, opts);
    const data = await res.json();
    return { ok: res.ok, status: res.status, data };
  } catch (err) {
    return { ok: false, error: err.message };
  }
}

async function run() {
  console.log('=== [NJANGI] PEUPLEMENT DONNÉES RÉELLES MULTI-SERVICES (PORT 9090) ===');

  const users = [
    { role: 'President', nom: 'Mbarga', prenom: 'Jean-Paul', tel: '+237699123456', email: 'jp.mbarga@njangi.cm', ville: 'Douala' },
    { role: 'Tresoriere', nom: 'Ngo Bikoi', prenom: 'Chantal', tel: '+237677889900', email: 'chantal.ngo@njangi.cm', ville: 'Douala' },
    { role: 'Secretaire', nom: 'Kamga', prenom: 'Pierre', tel: '+237655443322', email: 'pierre.kamga@njangi.cm', ville: 'Douala' },
    { role: 'Createur', nom: 'Etoo Fils', prenom: 'Samuel', tel: '+237699001122', email: 'samuel.etoo@njangi.cm', ville: 'Yaounde' },
    { role: 'Membre1', nom: 'Fokou', prenom: 'Rodrigue', tel: '+237670112233', email: 'rodrigue.fokou@njangi.cm', ville: 'Douala' },
    { role: 'Membre2', nom: 'Fotso', prenom: 'Christelle', tel: '+237691223344', email: 'christelle.fotso@njangi.cm', ville: 'Bafoussam' }
  ];

  const authData = {};
  for (const u of users) {
    let r = await api('/auth/inscrire', 'POST', {
      nom: u.nom,
      prenom: u.prenom,
      telephone: u.tel,
      email: u.email,
      motDePasse: 'Njangi2026!Securite',
      ville: u.ville,
      pays: 'Cameroun'
    });
    if (!r.ok) {
      r = await api('/auth/connexion', 'POST', { identifiant: u.tel, motDePasse: 'Njangi2026!Securite' });
    }
    if (r.data && r.data.data) {
      authData[u.role] = {
        token: r.data.data.accessToken,
        userId: r.data.data.utilisateur.id,
        user: r.data.data.utilisateur
      };
      console.log(' [1/8] Auth: ' + u.prenom + ' ' + u.nom + ' (' + u.tel + ')');
    }
  }

  const token = authData['President'].token;

  // 2. Profils DDD Membres
  const membres = {};
  for (const u of users) {
    const userId = authData[u.role].userId;
    let r = await api('/membres', 'POST', {
      authUtilisateurId: userId,
      nom: u.nom,
      prenom: u.prenom,
      telephone: u.tel,
      email: u.email,
      ville: u.ville
    }, token);

    if (!r.ok) {
      const all = await api('/membres', 'GET', null, token);
      if (Array.isArray(all.data)) {
        const found = all.data.find(m => m.telephone === u.tel);
        if (found) r = { ok: true, data: found };
      }
    }
    if (r.data) {
      const memId = r.data.id;
      membres[u.role] = { id: memId, ...u };
      console.log(' [2/8] Membre DDD: ' + u.prenom + ' ' + u.nom + ' -> ' + memId);
    }
  }

  // 3. Creer les Groupes
  const allGrp = await api('/groupes', 'GET', null, token);
  const existingList = (allGrp.data && allGrp.data.data) || [];

  let groupePrincipal = existingList.find(g => g.nom === 'Solidarite Douala Akwa');
  if (!groupePrincipal) {
    const res = await api('/groupes', 'POST', {
      nom: 'Solidarite Douala Akwa',
      description: 'Tontine dentraide et epargne rotative pour entrepreneurs et cadres du Littoral',
      typeSiege: 'ROTATIF',
      adresseSiege: 'Siege Tournant chez les membres',
      frequenceReunion: 'MENSUELLE',
      montantCotisationPrincipale: 100000,
      createurMembreId: membres['Createur'].id,
      nombreMembresMax: 15
    }, token);
    groupePrincipal = res.data.data;
  }
  console.log(' [3/8] Groupe Principal: ' + groupePrincipal.nom + ' (ID: ' + groupePrincipal.id + ')');

  // 4. Inscription des 6 membres dans le groupe
  for (const u of users) {
    const mid = membres[u.role].id;
    await api('/groupes/' + groupePrincipal.id + '/membres', 'POST', {
      membreId: mid,
      roleDansGroupe: u.role === 'Createur' ? 'CREATEUR' : 'MEMBRE'
    }, token);
  }

  // 5. Session
  let sessionId = null;
  const actSess = await api('/groupes/' + groupePrincipal.id + '/sessions/active', 'GET', null, token);
  if (actSess.data && actSess.data.data) {
    sessionId = actSess.data.data.id;
  } else {
    const sessRes = await api('/groupes/' + groupePrincipal.id + '/sessions', 'POST', {
      libelle: 'Session Annuelle 2026-2027',
      dateDebut: '2026-02-01',
      dateFin: '2027-01-31',
      montantCagnotteParSeance: 600000,
      nombreToursTotal: 6
    }, token);
    if (sessRes.ok && sessRes.data && sessRes.data.data) {
      sessionId = sessRes.data.data.id;
      await api('/groupes/sessions/' + sessionId + '/demarrer', 'PUT', null, token);
    }
  }
  console.log(' [4/8] Session Active: ' + sessionId);

  // 6. Bureau Executif
  await api('/groupes/' + groupePrincipal.id + '/bureau/election', 'POST', {
    presidentMembreId: membres['President'].id,
    tresorierMembreId: membres['Tresoriere'].id,
    secretaireMembreId: membres['Secretaire'].id,
    dateDebut: '2026-02-01',
    dateFin: '2028-01-31'
  }, token);

  // 7. Types de cotisation
  const caisses = [
    { libelle: 'Pot Rotatif Principal', desc: 'Cagnotte rotative mensuelle', cat: 'ROTATIVE_POT', montant: 100000, rotatif: true, oblig: true },
    { libelle: 'Caisse de Secours et Deces', desc: 'Assistance solidaire communautaire', cat: 'SECOURS_DECES', montant: 5000, rotatif: false, oblig: true },
    { libelle: 'Fonds de Reserve et Epargne', desc: 'Fonds dinvestissement commun', cat: 'CAISSE_RESERVE', montant: 2000, rotatif: false, oblig: true }
  ];
  for (const c of caisses) {
    await api('/cotisations/types', 'POST', {
      groupeId: groupePrincipal.id,
      libelle: c.libelle,
      description: c.desc,
      categorie: c.cat,
      montant: c.montant,
      estRotatif: c.rotatif,
      estObligatoire: c.oblig
    }, token);
  }
  console.log(' [5/8] Caisses Multi-Cotisations configurées (Pot, Secours, Réserve)');

  // 8. Reunion
  const allReu = await api('/reunions/groupe/' + groupePrincipal.id, 'GET', null, token);
  let reunionId = null;
  if (allReu.data && allReu.data.data && allReu.data.data.length > 0) {
    reunionId = allReu.data.data[0].id;
  } else {
    const reuRes = await api('/reunions', 'POST', {
      groupeId: groupePrincipal.id,
      sessionTontineId: sessionId,
      titre: 'Seance Mensuelle #1 - Rentree & Attributions',
      dateReunion: '2026-09-15T17:30:00',
      lieuReunion: 'Residence Mbarga, Bonapriso, Douala',
      typeSiege: 'ROTATIF',
      hoteId: membres['President'].id,
      ordreJour: '1. Ouverture et priere\n2. Emargement\n3. Cotisations\n4. Attributions pot\n5. Divers',
      presidentReunionId: membres['President'].id,
      secretaireReunionId: membres['Secretaire'].id,
      tresorierReunionId: membres['Tresoriere'].id
    }, token);
    if (reuRes.data && reuRes.data.data) reunionId = reuRes.data.data.id;
  }
  console.log(' [6/8] Reunion ID: ' + reunionId);

  // 9. Tours de Pot
  if (sessionId) {
    const ordre = [
      membres['President'].id,
      membres['Tresoriere'].id,
      membres['Secretaire'].id,
      membres['Createur'].id,
      membres['Membre1'].id,
      membres['Membre2'].id
    ];
    await api('/cotisations/pots/planifier', 'POST', {
      groupeId: groupePrincipal.id,
      sessionTontineId: sessionId,
      ordreBeneficiaires: ordre,
      montantParTour: 600000
    }, token);
    console.log(' [7/8] Ordre de passage du pot rotatif planifié pour les 6 membres');
  }

  // 10. Cotisations pour la reunion
  if (reunionId) {
    const allMemIds = users.map(u => membres[u.role].id);
    const genRes = await api('/cotisations/reunion/generer', 'POST', {
      groupeId: groupePrincipal.id,
      reunionId: reunionId,
      membreIds: allMemIds,
      dateLimitePaiement: '2026-09-15'
    }, token);

    // Paiements reels Cash et Mobile Money
    const cots = await api('/cotisations/reunion/' + reunionId, 'GET', null, token);
    const cotList = (cots.data && cots.data.data) || [];

    // Cotisation President -> Paiement Cash avec recu
    const cotPres = cotList.find(c => c.membreId === membres['President'].id);
    if (cotPres) {
      const payCash = await api('/paiements/cash', 'POST', {
        cotisationId: cotPres.id,
        membreId: membres['President'].id,
        groupeId: groupePrincipal.id,
        montant: cotPres.montantAttendu,
        cleIdempotence: 'idemp-cash-pres-001',
        pieceJointeUrl: 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?fit=crop&w=600&h=400&q=80',
        commentaire: 'Reglement especes effectue en seance a la tresoriere'
      }, token);

      if (payCash.data && payCash.data.data) {
        // Valider cash par la tresoriere
        await api('/paiements/' + payCash.data.data.id + '/valider-cash', 'PUT', {
          valide: true,
          commentaireValidation: 'Recu de versement conforme signe'
        }, authData['Tresoriere'].token);
        console.log('  * Paiement Cash valide par Tresoriere avec recu signe');
      }
    }

    // Cotisation Tresoriere -> Paiement Mobile Money MTN
    const cotTres = cotList.find(c => c.membreId === membres['Tresoriere'].id);
    if (cotTres) {
      await api('/paiements/mobile-money', 'POST', {
        cotisationId: cotTres.id,
        membreId: membres['Tresoriere'].id,
        groupeId: groupePrincipal.id,
        montant: cotTres.montantAttendu,
        numeroTelephone: '+237677889900',
        operateur: 'MTN',
        cleIdempotence: 'idemp-momo-001',
        commentaire: 'Reglement MTN Mobile Money'
      }, token);
      console.log('  * Paiement MTN MoMo effectue');
    }
  }

  // 11. Penalites
  const penRetard = await api('/penalites', 'POST', {
    membreId: membres['Secretaire'].id,
    groupeId: groupePrincipal.id,
    sessionId: sessionId,
    reunionId: reunionId,
    typeInfraction: 'RETARD_REUNION',
    montant: 1000,
    motif: 'Arrivee tardive a la seance (18h15)'
  }, token);

  if (penRetard.data && penRetard.data.data) {
    await api('/penalites/' + penRetard.data.data.id + '/payer', 'PUT', null, token);
    console.log('  * Penalite retard 1 000 XAF appliquee et reglee');
  }

  await api('/penalites', 'POST', {
    membreId: membres['Membre2'].id,
    groupeId: groupePrincipal.id,
    sessionId: sessionId,
    reunionId: reunionId,
    typeInfraction: 'ABSENCE',
    montant: 5000,
    motif: 'Absence sans mot dexcuse'
  }, token);
  console.log('  * Penalite absence 5 000 XAF appliquee en attente');

  // 12. Recalculer Statistiques
  if (sessionId) {
    await api('/statistiques/groupe/' + groupePrincipal.id + '/session/' + sessionId + '/calculer', 'POST', null, token);
    console.log(' [8/8] Statistiques de caisse et de presence recalculees');
  }

  console.log('=== [SUCCES TOTAL] TOUTES LES DONNEES METIER REELLES SONT EN BASE DE DONNEES POSTGRESQL ===');
}

run().catch(console.error);
