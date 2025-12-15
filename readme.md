# 🧵 Threaded Job Scheduler

Un **job scheduler multithreadé** écrit en **Java 25**, conçu comme un **projet personnel orienté architecture concurrente**.

Ce projet a pour objectif de démontrer une **maîtrise avancée du multithreading**, des **structures concurrentes**, des **machines à états atomiques**, et des **problèmes réels rencontrés en production** (retry, starvation, workers qui meurent silencieusement, transitions d’état invalides, etc.).

---

## 🚀 Objectifs du projet

- Implémenter un scheduler de jobs **thread-safe** sans dépendre d’un framework externe
- Gérer plusieurs workers concurrents
- Supporter :
  - priorités de jobs
  - retry contrôlé
  - timeout d’exécution
  - transitions d’état atomiques
- Mettre en évidence les **pièges classiques du multithreading** et leurs solutions

---

## 🧠 Concepts clés démontrés

- `PriorityBlockingQueue` pour l’ordonnancement
- Workers dédiés (threads longs vivants)
- Séparation claire des responsabilités :
  - **Scheduler** → orchestration
  - **Worker** → exécution
  - **Job** → machine à états
- Gestion correcte des retries sans bloquer de thread
- Utilisation de `AtomicReference` et `compareAndSet`
- Robustesse face aux exceptions non contrôlées

---

## 🏗️ Architecture globale

```
┌──────────────┐
│ JobScheduler │
│              │
│  submit()    │
│  retry logic │
└───────┬──────┘
        │
        ▼
┌──────────────────────────┐
│ PriorityBlockingQueue    │
│  (readyQueue)            │
└──────────┬───────────────┘
           │
     ┌─────┴─────┐
     ▼           ▼
┌──────────┐ ┌──────────┐
│ Worker-1 │ │ Worker-2 │  ...
└──────────┘ └──────────┘
```

---

## 🧩 Machine à états d’un Job

```text
PENDING → READY → RUNNING → SUCCESS
                   ↘
                    READY (retry)
                   ↘
                   FAILED (terminal)
```

### États
- `PENDING` : job créé mais pas encore planifié
- `READY` : prêt à être exécuté
- `RUNNING` : en cours d’exécution par un worker
- `SUCCESS` : terminé avec succès (terminal)
- `FAILED` : échec définitif après épuisement des retries (terminal)

👉 Les transitions sont **atomiques** et validées via `compareAndSet`.

---

## 🔁 Gestion du retry

- Le retry est **non bloquant**
- Aucun thread n’est immobilisé pour attendre un retry
- Un job en échec est simplement remis dans la `readyQueue`

### Point important

Un job peut repasser de `FAILED → READY` **tant que le nombre maximum de tentatives n’est pas atteint**.

```java
public boolean markReady() {
    return state.compareAndSet(PENDING, READY)
        || state.compareAndSet(FAILED, READY);
}
```

Ce choix est **volontairement documenté** afin d’éviter toute ambiguïté sur la nature terminale de l’état `FAILED`.

---

## 🧵 Gestion des workers

- Chaque worker est un thread dédié
- Les workers ne meurent jamais silencieusement
- Toute exception est interceptée pour garantir la continuité du pool

```java
try {
    execute(job);
} catch (Throwable t) {
    log.error("Worker crashed", t);
}
```

👉 Ceci évite un état où la queue fonctionne mais où plus aucun thread ne consomme les jobs.

---

## ⏱️ Timeout d’exécution

- Les jobs sont exécutés via un `ExecutorService`
- Chaque job peut être interrompu s’il dépasse le timeout configuré

---

## 🧪 Tests

- Tests multi-threadés
- Cas couverts :
  - retry avec plusieurs workers
  - starvation
  - timeout
  - échec définitif
  - concurrence forte

---

## ⚙️ Technologies

- Java **25**
- `java.util.concurrent`
- Aucune dépendance externe

---

## 🧑‍💻 Auteur

Projet personnel développé pour démontrer des compétences avancées en **concurrence Java** et en **design de systèmes multithreadés**.

---

✅ **Ce projet est volontairement pédagogique, robuste et orienté production.**

