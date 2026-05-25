# Question 3 - Architecture en couches

## a) Le patron architectural en couches

### Le problème

Quand on développe un système logiciel de taille réelle, on se retrouve très vite face à un défi fondamental : **comment organiser le code pour qu'il reste maintenable, évolutif et compréhensible sur le long terme ?**

Sans organisation claire, les systèmes ont tendance à évoluer vers ce qu'on appelle souvent un "big ball of mud" : une masse de code où tout dépend de tout. Une classe de présentation appelle directement la base de données, une règle métier est éparpillée dans dix fichiers différents, et changer un détail d'affichage peut casser une règle de gestion. Ce **couplage fort** entre les préoccupations rend le système :

- difficile à tester (on ne peut pas isoler un composant),
- difficile à faire évoluer (changer une technologie impacte tout le reste),
- difficile à comprendre (aucune séparation claire des responsabilités),
- instable dès qu'on modifie quelque chose (les effets de bord sont imprévisibles).

### La solution

Le patron **Architecture en couches** (Layered Architecture) consiste à organiser le système en groupes horizontaux de composants (les couches) où **chaque couche a une responsabilité précise et cohérente**. La règle principale est simple : une couche ne communique qu'avec la couche immédiatement en dessous d'elle. On parle de dépendance unidirectionnelle descendante.

Cette organisation force une **séparation des préoccupations** (Separation of Concerns) claire : la présentation ne sait pas comment les données sont stockées, la logique métier ne sait pas dans quelle interface elle sera affichée. Chaque couche expose une interface stable vers le haut et s'appuie sur les services de la couche inférieure.

### Discussion

Le patron en couches est l'un des plus anciens et des plus répandus en architecture logicielle, et il y a de bonnes raisons à ça. Il est **intuitif** : les équipes comprennent naturellement la séparation entre ce qui est affiché, ce qui est calculé, et ce qui est persisté.

Cependant, il présente aussi quelques limites. Dans les systèmes très stricts où on respecte à la lettre la règle "une couche ne parle qu'à sa voisine directe", on peut se retrouver à créer des couches intermédiaires qui ne font que passer des données sans vraiment les transformer (c'est ce qu'on appelle le _sinkhole anti-pattern_). De plus, si on a besoin de fonctionnalités transversales (logging, sécurité, gestion des erreurs), elles ne rentrent pas naturellement dans une seule couche et peuvent se retrouver dupliquées.

Malgré ces limites, pour la grande majorité des systèmes d'information d'entreprise, l'architecture en couches reste une base solide et éprouvée.

---

## b) Rôle de chaque couche

La figure du cours présente six couches, du plus spécifique à l'application (en haut) au plus générique et technique (en bas).

---

### 1. Couche Présentation _(Presentation / Interface / UI / View)_

C'est la couche avec laquelle l'utilisateur interagit directement. Elle est responsable de **l'affichage des données** et de la **capture des actions utilisateur**. Elle ne contient aucune logique métier : son seul rôle est de présenter l'information de façon compréhensible et de transmettre les actions vers la couche inférieure.

Ce qui la caractérise : elle est hautement volatile. Les interfaces changent souvent, les technologies évoluent (d'une application desktop on passe au web, puis au mobile), et c'est précisément pour ça qu'elle doit être complètement isolée du reste.

---

### 2. Couche Application _(Application / Workflow / Process / App Controller)_

Cette couche agit comme un **chef d'orchestre**. Elle ne contient pas de règles métier à proprement parler, mais elle coordonne les flux de traitement entre la présentation et le domaine. C'est ici qu'on gère le déroulement d'un processus utilisateur (workflow), les transitions entre pages ou fenêtres, et l'état de la session.

Elle joue aussi un rôle important de **transformation et consolidation** : les données venant de plusieurs sources du domaine peuvent être agrégées et reformatées pour répondre aux besoins de la présentation.

---

### 3. Couche Domaine _(Domain / Business / Business Services / Model)_

C'est le **cœur du système**. La couche domaine contient les règles métier spécifiques à l'application, les entités du modèle objet, et la logique qui fait que le système a de la valeur. C'est ici que se trouvent les concepts du monde réel que le système modélise (un client, une commande, un paiement, un inventaire).

Ces règles peuvent parfois être partagées entre plusieurs applications (un service de gestion des stocks peut servir à la fois l'application de vente et l'application de logistique), mais elles restent spécifiques au domaine métier de l'entreprise.

---

### 4. Couche Infrastructure Métier _(Business Infrastructure / Low-level Business Services)_

Cette couche est souvent oubliée dans les architectures simplifiées, mais elle joue un rôle important : elle contient des **services métier de très bas niveau et très génériques**, utilisables dans de nombreux domaines métier différents, pas seulement dans l'application courante.

La différence avec la couche Domaine est subtile mais réelle : là où le Domaine contient des règles spécifiques à _ce_ système (ex. les règles de tarification d'une location de voiture), l'Infrastructure Métier contient des services réutilisables dans n'importe quel contexte commercial (ex. la conversion de devises, le calcul de taxes standard, la gestion d'un calendrier).

---

### 5. Couche Services Techniques _(Technical Services / Technical Infrastructure / High-level Technical Services)_

On entre maintenant dans les couches purement techniques, sans aucune notion métier. Cette couche fournit des **services techniques de haut niveau et des frameworks** qui servent d'infrastructure aux couches supérieures.

Ce sont des composants réutilisables dans n'importe quel type d'application, qu'il s'agisse d'un système de location de voitures ou d'une application bancaire. Ils résolvent des problèmes techniques récurrents.

---

### 6. Couche Fondation _(Foundation / Core Services / Base Services / Low-level Technical Services)_

C'est la couche la plus basse, le socle sur lequel tout le reste repose. Elle fournit des **utilitaires et services techniques de très bas niveau** : les briques élémentaires du développement logiciel.

Ces composants n'ont aucune connaissance du métier, ni même d'une application particulière. Ils sont complètement génériques et stables.

---

## c) Exemples de packages pour chaque couche

Pour illustrer concrètement, prenons l'exemple d'un **système de gestion bancaire en ligne**.

| Couche                    | Package(s) exemple                                                                                                                                                |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Présentation**          | `com.banque.ui.web` : contrôleurs Spring MVC, templates Thymeleaf, composants JavaScript React pour le tableau de bord client                                     |
| **Application**           | `com.banque.app.virement` : orchestration du flux de virement (validation du formulaire, vérification des droits, appel domaine, confirmation à l'utilisateur) |
| **Domaine**               | `com.banque.domain.compte` : entités `Compte`, `Transaction`, `Client` ; services `CalculInteret`, `VerificationPlafond`, règles de fraude                        |
| **Infrastructure Métier** | `com.banque.business.devises` : service `ConvertisseurDevises` utilisant des taux de change ; `CalculateurTauxInteret` basé sur des barèmes standards             |
| **Services Techniques**   | `com.banque.tech.persistence` : couche JPA/Hibernate ; `com.banque.tech.security` : gestion des tokens JWT, chiffrement, audit                                    |
| **Fondation**             | `com.banque.foundation.utils` : utilitaires de manipulation de dates, de chaînes de caractères, logging bas niveau, pool de connexions JDBC                       |

---

## d) Comment ces couches contribuent à une architecture modulaire et cohésive

### Modularité

La modularité désigne la capacité à décomposer un système en parties indépendantes que l'on peut modifier, remplacer ou faire évoluer séparément.

L'architecture en couches y contribue de plusieurs façons concrètes :

**Isolation des changements technologiques.** Si demain on décide de passer d'une interface web à une application mobile, seule la couche Présentation est impactée. Les règles métier du Domaine restent intactes. De même, si on remplace Oracle par PostgreSQL, seule la couche Services Techniques (persistance) est touchée, et les couches supérieures ne voient pas la différence.

**Remplacement de composants.** Chaque couche expose une interface vers la couche supérieure. Tant que cette interface reste stable, l'implémentation interne peut changer complètement. On peut par exemple remplacer un framework de sécurité par un autre sans toucher à la logique applicative.

**Développement en parallèle.** Des équipes différentes peuvent travailler sur des couches différentes simultanément, à condition de se mettre d'accord sur les interfaces entre couches. Cela accélère considérablement le développement sur des projets de grande taille.

### Cohésion

La cohésion désigne le fait que les éléments d'un même module ont une responsabilité commune et travaillent ensemble vers le même objectif.

Les couches forcent naturellement une **cohésion fonctionnelle forte** : tout ce qui concerne l'affichage est dans la Présentation, tout ce qui concerne les règles métier est dans le Domaine. On évite ainsi les classes "fourre-tout" qui mélangent des préoccupations sans rapport.

En pratique, quand un développeur cherche où corriger un bug d'affichage, il sait exactement dans quelle couche chercher. Quand une règle de gestion change, il sait que la modification sera localisée dans le Domaine. Cette **prévisibilité** est une des conséquences directes d'une haute cohésion.

### Réduction du couplage

Le principe de dépendance unidirectionnelle (une couche ne dépend que de la couche en dessous) limite mécaniquement le couplage. La couche Présentation ne connaît pas l'existence de la couche Fondation et ne peut donc pas en dépendre directement. Ce cloisonnement réduit le nombre de relations entre composants et rend le système plus stable : une modification en bas de la pile ne peut se propager vers le haut que via les interfaces définies, pas de façon incontrôlée.

**En résumé**, le patron en couches n'est pas une recette magique, mais il offre un cadre structurant qui, s'il est respecté, permet de construire des systèmes qui restent compréhensibles et maintenables même des années après leur conception initiale. C'est particulièrement précieux dans les systèmes d'information d'entreprise qui ont une durée de vie longue et qui évoluent en permanence.
