# Architecture Microservices — Application Bancaire

**Cours** : Architecture d'Entreprise  
**Université** : UQAC  
**Stack** : Java 17 · Spring Boot 3.2 · Spring Cloud 2023 · H2 · Docker

---

## Vue d'ensemble

Application bancaire distribuée en six microservices indépendants, communiquant via un registre de services (Eureka) et une passerelle centralisée (API Gateway). Chaque service possède sa propre base de données en mémoire (H2), conformément au principe *database per service*.

```
Client
  │
  ▼
api-gateway          :9080   — routage, point d'entrée unique
  ├── auth-service         :9081   — authentification JWT
  ├── account-service      :8082   — gestion des comptes
  ├── transaction-service  :8083   — opérations financières
  └── notification-service :8084   — journalisation des événements

discovery-service    :8761   — registre Eureka (infrastructure)
```

---

## Services

| Service | Port | Responsabilité | Base |
|---|---|---|---|
| `discovery-service` | 8761 | Registre Eureka | — |
| `api-gateway` | 9080 | Routage HTTP | — |
| `auth-service` | 9081 | Register / Login / JWT | `authdb` |
| `account-service` | 8082 | CRUD comptes bancaires | `accountdb` |
| `transaction-service` | 8083 | Dépôt, retrait, virement | `transactiondb` |
| `notification-service` | 8084 | Log des événements | `notificationdb` |

---

## Communication inter-services

`transaction-service` invoque `account-service` et `notification-service` via **OpenFeign** avec résolution d'adresse dynamique par Eureka :

```
transaction-service
  ├── AccountClient   → lb://account-service   (vérif. et mise à jour des soldes)
  └── NotificationClient → lb://notification-service  (émission d'événements)
```

---

## Démarrage

**Prérequis** : Docker + Docker Compose installés.

```bash
git clone <repo>
cd banking-microservices
docker-compose up --build
```

Ordre de démarrage géré automatiquement par les `depends_on` et les `healthcheck`.  
Durée initiale : ~3–5 min (build Maven inclus).

**Vérification** : [http://localhost:8761](http://localhost:8761) — les six services doivent apparaître dans le tableau de bord Eureka.

---

## API Reference

Tous les appels passent par la gateway sur `http://localhost:9080`.

### Auth

```bash
# Inscription
POST /auth/register
{ "username": "alice", "email": "alice@bank.fr", "password": "secret123" }

# Connexion → retourne un JWT
POST /auth/login
{ "username": "alice", "password": "secret123" }
```

### Comptes

```bash
# Créer un compte
POST /accounts
{ "userId": 1, "ownerName": "Alice Dupont", "initialBalance": 1000.00, "accountType": "CHECKING" }

# Consulter un compte
GET /accounts/{id}

# Comptes d'un utilisateur
GET /accounts/user/{userId}
```

### Transactions

```bash
# Dépôt
POST /transactions/deposit
{ "accountId": 1, "amount": 250.00, "description": "Salaire" }

# Retrait
POST /transactions/withdraw
{ "accountId": 1, "amount": 100.00, "description": "Courses" }

# Virement
POST /transactions/transfer
{ "sourceAccountId": 1, "targetAccountId": 2, "amount": 200.00, "description": "Epargne" }

# Historique
GET /transactions/{accountId}
```

### Notifications

```bash
GET /notifications                    # toutes les notifications
GET /notifications/account/{id}       # par compte
```

---

## Structure du projet

```
banking-microservices/
├── docker-compose.yml
├── discovery-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../DiscoveryApplication.java
│       └── resources/application.yml
├── api-gateway/
│   ├── Dockerfile · pom.xml
│   └── src/main/
│       ├── java/.../GatewayApplication.java
│       └── resources/application.yml
├── auth-service/
│   ├── Dockerfile · pom.xml
│   └── src/main/java/.../
│       ├── AuthApplication.java
│       ├── controller/AuthController.java
│       ├── service/AuthService.java
│       ├── model/User.java
│       ├── dto/{LoginRequest, RegisterRequest, AuthResponse}.java
│       ├── repository/UserRepository.java
│       └── security/{JwtUtil, SecurityConfig}.java
├── account-service/
│   └── src/main/java/.../
│       ├── model/Account.java
│       ├── dto/{AccountRequest, AccountResponse, BalanceUpdateRequest}.java
│       ├── repository/AccountRepository.java
│       ├── service/AccountService.java
│       └── controller/AccountController.java
├── transaction-service/
│   └── src/main/java/.../
│       ├── model/Transaction.java
│       ├── dto/{TransactionRequest, TransactionResponse, AccountDto, ...}.java
│       ├── client/{AccountClient, NotificationClient}.java   ← Feign
│       ├── repository/TransactionRepository.java
│       ├── service/TransactionService.java
│       └── controller/TransactionController.java
└── notification-service/
    └── src/main/java/.../
        ├── model/Notification.java
        ├── dto/{NotificationRequest, NotificationResponse}.java
        ├── repository/NotificationRepository.java
        ├── service/NotificationService.java
        └── controller/NotificationController.java
```

---

## Arrêt

```bash
docker-compose down            # arrête les conteneurs
docker-compose down --rmi local  # supprime aussi les images buildées
```
