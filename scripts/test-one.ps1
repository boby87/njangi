# Connexion ou Inscription
$inscr = @{
    nom = 'Etoo'
    prenom = 'Samuel'
    telephone = '+237699001122'
    email = 'samuel.etoo@njangi.cm'
    motDePasse = 'Njangi2026!Securite'
} | ConvertTo-Json

try {
    $authRes = Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/auth/inscrire' -Method Post -Body $inscr -ContentType 'application/json'
    $token = $authRes.data.accessToken
    $userId = $authRes.data.utilisateur.id
} catch {
    $login = @{ identifiant = '+237699001122'; motDePasse = 'Njangi2026!Securite' } | ConvertTo-Json
    $authRes = Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/auth/connexion' -Method Post -Body $login -ContentType 'application/json'
    $token = $authRes.data.accessToken
    $userId = $authRes.data.utilisateur.id
}

Write-Host 'Auth UserId:' $userId
$headers = @{ Authorization = 'Bearer ' + $token }

# Membre profil
$membreBody = @{
    authUtilisateurId = $userId
    nom = 'Etoo'
    prenom = 'Samuel'
    telephone = '+237699001122'
    email = 'samuel.etoo@njangi.cm'
    ville = 'Yaounde'
} | ConvertTo-Json

try {
    $memRes = Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/membres' -Method Post -Headers $headers -Body $membreBody -ContentType 'application/json'
    $membreId = $memRes.id
} catch {
    $allMem = Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/membres' -Method Get -Headers $headers
    $m = $allMem | Where-Object { $_.telephone -eq '+237699001122' }
    $membreId = $m.id
}

Write-Host 'MembreId:' $membreId

# Creer Groupe
$grpBody = @{
    nom = 'Solidarite Douala Akwa'
    description = 'Tontine dentraide et epargne rotative pour entrepreneurs'
    typeSiege = 'ROTATIF'
    adresseSiege = 'Siege Tournant'
    frequenceReunion = 'MENSUELLE'
    montantCotisationPrincipale = 100000
    createurMembreId = $membreId
    nombreMembresMax = 15
} | ConvertTo-Json

$grpRes = Invoke-RestMethod -Uri 'http://localhost:9090/api/v1/groupes' -Method Post -Headers $headers -Body $grpBody -ContentType 'application/json'
Write-Host 'GROUPE SUCCESS:'
$grpRes | ConvertTo-Json -Depth 5
