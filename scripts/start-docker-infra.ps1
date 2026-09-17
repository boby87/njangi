# scripts/start-docker-infra.ps1
# Script de demarrage et verification de l'infrastructure Docker Njangi (Postgres, Kafka KRaft, Eureka)

[CmdletBinding()]
param(
    [int]$TimeoutSeconds = 60
)

$ErrorActionPreference = "Stop"

function Write-Step([string]$message) {
    Write-Host "`n[STEP] $message" -ForegroundColor Cyan
}

function Write-Success([string]$message) {
    Write-Host "[OK] $message" -ForegroundColor Green
}

function Write-WarningMsg([string]$message) {
    Write-Host "[WARN] $message" -ForegroundColor Yellow
}

function Write-ErrorMsg([string]$message) {
    Write-Host "[ERROR] $message" -ForegroundColor Red
}

Write-Host "============================================================" -ForegroundColor Green
Write-Host "  NJANGI PLATFORM - DEMARRAGE DE L'INFRASTRUCTURE DOCKER    " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

# 1. Verifier si le daemon Docker repond
Write-Step "Verification de l'etat du daemon Docker..."

$dockerRunning = $false
try {
    $null = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        $dockerRunning = $true
        Write-Success "Docker daemon est deja actif."
    }
} catch {
    $dockerRunning = $false
}

if (-not $dockerRunning) {
    Write-WarningMsg "Le daemon Docker n'est pas actif. Tentative de demarrage de Docker Desktop..."
    $dockerDesktopPath = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    
    if (Test-Path $dockerDesktopPath) {
        Start-Process -FilePath $dockerDesktopPath
        Write-Host "Docker Desktop lance. Attente de l'initialisation du moteur..." -ForegroundColor Yellow
        
        $elapsed = 0
        while ($elapsed -lt $TimeoutSeconds) {
            Start-Sleep -Seconds 3
            $elapsed += 3
            try {
                $null = docker info 2>&1
                if ($LASTEXITCODE -eq 0) {
                    $dockerRunning = $true
                    Write-Success "Docker daemon est operationnel !"
                    break
                }
            } catch {}
            Write-Host "En attente de Docker Desktop ($elapsed s / $TimeoutSeconds s)..." -ForegroundColor DarkGray
        }
    } else {
        Write-ErrorMsg "Docker Desktop est introuvable a l'emplacement standard ($dockerDesktopPath)."
        Write-Host "Veuillez demarrer Docker manuellement." -ForegroundColor Yellow
        exit 1
    }
}

if (-not $dockerRunning) {
    Write-ErrorMsg "Docker n'a pas repondu dans le delai imparti ($TimeoutSeconds secondes)."
    exit 1
}

# 2. Demarrage des conteneurs de base
Write-Step "Lancement des conteneurs : PostgreSQL, Kafka KRaft, Eureka Server..."
$composeFile = Join-Path $PSScriptRoot "..\docker-compose.yml"

docker compose -f $composeFile up -d postgres kafka eureka-server

if ($LASTEXITCODE -ne 0) {
    Write-ErrorMsg "Echec de l'execution de docker compose up."
    exit 1
}

Write-Success "Conteneurs demarres via Docker Compose."

# 3. Verification des ports d'ecoute
Write-Step "Controle d'accessibilite reseau des services..."

$services = @(
    @{ Name = "PostgreSQL (9 schemas)"; Port = 5432 },
    @{ Name = "Kafka KRaft"; Port = 9092 },
    @{ Name = "Eureka Server"; Port = 8761 }
)

foreach ($s in $services) {
    $portOpen = $false
    $retries = 10
    while ($retries -gt 0) {
        $tcp = Test-NetConnection -ComputerName "localhost" -Port $s.Port -WarningAction SilentlyContinue
        if ($tcp.TcpTestSucceeded) {
            $portOpen = $true
            break
        }
        Start-Sleep -Seconds 2
        $retries--
    }

    if ($portOpen) {
        Write-Success "$($s.Name) est actif et ecoute sur le port $($s.Port)."
    } else {
        Write-WarningMsg "$($s.Name) ne repond pas encore sur le port $($s.Port) (en cours d'initialisation)."
    }
}

Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "  INFRASTRUCTURE DOCKER PRETE POUR LES TESTS D'INTEGRATION   " -ForegroundColor Green
Write-Host "============================================================`n" -ForegroundColor Green
