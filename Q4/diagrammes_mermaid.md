# Diagrammes Mermaid — Application Bancaire Microservices

## Figure 1 : Diagramme d'architecture globale

```mermaid
graph TB
    Client(["🖥️ Client HTTP"]):::client --> GW["🔀 API Gateway<br/>:8080"]:::gateway
    
    GW --> AUTH["🔐 Auth Service<br/>:8081"]:::service
    GW --> ACC["🏦 Account Service<br/>:8082"]:::service
    GW --> TXN["💸 Transaction Service<br/>:8083"]:::service
    GW --> NOTIF["🔔 Notification Service<br/>:8084"]:::service
    
    TXN -->|"OpenFeign"| ACC
    TXN -->|"OpenFeign"| NOTIF
    
    AUTH --> DB1[("authdb<br/>H2")]:::db
    ACC --> DB2[("accountdb<br/>H2")]:::db
    TXN --> DB3[("transactiondb<br/>H2")]:::db
    NOTIF --> DB4[("notificationdb<br/>H2")]:::db
    
    GW -.->|"register"| EUR["📋 Eureka Server<br/>:8761"]:::eureka
    AUTH -.->|"register"| EUR
    ACC -.->|"register"| EUR
    TXN -.->|"register"| EUR
    NOTIF -.->|"register"| EUR

    classDef client fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
    classDef gateway fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef service fill:#e8f5e9,stroke:#388e3c,stroke-width:2px
    classDef db fill:#fce4ec,stroke:#c62828,stroke-width:2px
    classDef eureka fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
```

---

## Figure 2 : Diagramme de séquence — Flux d'authentification

```mermaid
sequenceDiagram
    participant C as 🖥️ Client
    participant GW as 🔀 API Gateway
    participant AUTH as 🔐 Auth Service
    participant DB as 🗄️ authdb (H2)
    
    rect rgb(232, 245, 233)
        Note over C,DB: Inscription d'un nouvel utilisateur
        C->>GW: POST /auth/register<br/>{username, email, password}
        GW->>AUTH: Forward request
        AUTH->>DB: Vérifier unicité username/email
        DB-->>AUTH: OK (pas de doublon)
        AUTH->>AUTH: Hacher mot de passe (BCrypt)
        AUTH->>DB: INSERT user
        DB-->>AUTH: User sauvegardé (id=1)
        AUTH->>AUTH: Générer JWT (HS256)
        AUTH-->>GW: 201 {token, username, userId}
        GW-->>C: 201 Created
    end

    rect rgb(227, 242, 253)
        Note over C,DB: Connexion d'un utilisateur existant
        C->>GW: POST /auth/login<br/>{username, password}
        GW->>AUTH: Forward request
        AUTH->>DB: SELECT user WHERE username=?
        DB-->>AUTH: User trouvé
        AUTH->>AUTH: BCrypt.matches(password, hash)
        AUTH->>AUTH: Générer JWT (HS256)
        AUTH-->>GW: 200 {token, username, userId}
        GW-->>C: 200 OK
    end
```

---

## Figure 3 : Diagramme de séquence — Flux d'un virement bancaire

```mermaid
sequenceDiagram
    participant C as 🖥️ Client
    participant GW as 🔀 API Gateway
    participant TXN as 💸 Transaction Service
    participant ACC as 🏦 Account Service
    participant NOTIF as 🔔 Notification Service

    C->>GW: POST /transactions/transfer<br/>{sourceAccountId, targetAccountId, amount}
    GW->>TXN: Forward request

    rect rgb(255, 243, 224)
        Note over TXN,ACC: 1. Récupération des comptes
        TXN->>ACC: GET /accounts/{sourceId} (Feign)
        ACC-->>TXN: AccountDto (source)
        TXN->>ACC: GET /accounts/{targetId} (Feign)
        ACC-->>TXN: AccountDto (target)
    end

    rect rgb(232, 245, 233)
        Note over TXN: 2. Validations métier
        TXN->>TXN: Vérifier compte source actif
        TXN->>TXN: Vérifier compte cible actif
        TXN->>TXN: Vérifier solde suffisant
    end

    rect rgb(227, 242, 253)
        Note over TXN,ACC: 3. Mise à jour des soldes
        TXN->>ACC: PUT /accounts/{sourceId}/balance<br/>{amount, "WITHDRAW"} (Feign)
        ACC-->>TXN: OK (solde débité)
        TXN->>ACC: PUT /accounts/{targetId}/balance<br/>{amount, "DEPOSIT"} (Feign)
        ACC-->>TXN: OK (solde crédité)
    end

    rect rgb(243, 229, 245)
        Note over TXN: 4. Persistance
        TXN->>TXN: Sauvegarder Transaction<br/>(type=TRANSFER, status=SUCCESS)
    end

    rect rgb(255, 235, 238)
        Note over TXN,NOTIF: 5. Notifications
        TXN->>NOTIF: POST /notifications<br/>{TRANSFER_DEBIT, sourceAccountId} (Feign)
        NOTIF-->>TXN: 201 Created
        TXN->>NOTIF: POST /notifications<br/>{TRANSFER_CREDIT, targetAccountId} (Feign)
        NOTIF-->>TXN: 201 Created
    end

    TXN-->>GW: 201 {TransactionResponse}
    GW-->>C: 201 Created
```

---

## Figure 4 : Diagramme de séquence — Flux de notification

```mermaid
sequenceDiagram
    participant TXN as 💸 Transaction Service
    participant NOTIF as 🔔 Notification Service
    participant DB as 🗄️ notificationdb (H2)

    TXN->>NOTIF: POST /notifications<br/>{eventType, message, accountId, transactionId}
    
    rect rgb(232, 245, 233)
        Note over NOTIF: Traitement de la notification
        NOTIF->>NOTIF: log.info("[EVENT] eventType |<br/>Account: accountId |<br/>Transaction: txnId | message")
        NOTIF->>DB: INSERT notification<br/>(status="DELIVERED", createdAt=now())
        DB-->>NOTIF: Notification sauvegardée
    end
    
    NOTIF-->>TXN: 201 {NotificationResponse}
```

---

## Figure 5 : Diagramme de composants — Communication inter-services

```mermaid
graph LR
    subgraph "Infrastructure"
        EUR["Eureka Server"]
        GW["API Gateway"]
    end
    
    subgraph "Services Métier"
        AUTH["Auth Service"]
        ACC["Account Service"]
        TXN["Transaction Service"]
        NOTIF["Notification Service"]
    end
    
    subgraph "Données"
        DB1[("authdb")]
        DB2[("accountdb")]
        DB3[("transactiondb")]
        DB4[("notificationdb")]
    end
    
    GW ==>|"/auth/**"| AUTH
    GW ==>|"/accounts/**"| ACC
    GW ==>|"/transactions/**"| TXN
    GW ==>|"/notifications/**"| NOTIF
    
    TXN -->|"AccountClient<br/>(Feign)"| ACC
    TXN -->|"NotificationClient<br/>(Feign)"| NOTIF
    
    AUTH --- DB1
    ACC --- DB2
    TXN --- DB3
    NOTIF --- DB4
```
