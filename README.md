# Spese Manu & Leo

Progetto Android nativo in Kotlin per gestire le spese condivise tra Manu e Leo.

## Funzioni

- inserimento, modifica ed eliminazione delle spese;
- scelta rapida della ripartizione **50% Manu / 50% Leo** oppure **60% Manu / 40% Leo**;
- calcolo automatico degli importi dovuti;
- registrazione opzionale degli importi già pagati da Manu e Leo;
- riepilogo complessivo di importi dovuti e pagati;
- persistenza locale offline in un database SQLite;
- interfaccia Material con supporto tema chiaro/scuro.

## Dati iniziali importati da `Spese.xlsx`

La tabella Excel `TabellaSpese` contiene una riga iniziale, precaricata alla prima installazione:

| Data | Descrizione | Importo | Manu | Leo | Pagato Manu | Pagato Leo |
|---|---:|---:|---:|---:|---:|---:|
| 2026-11-01 | prova | €100,00 | 60% | 40% | €0,00 | €0,00 |

Il file Excel originale è incluso nella cartella `reference/` come documentazione della sorgente.

## Corrispondenza con le colonne Excel

| Excel | App / database |
|---|---|
| DATA SPESA | `expense_date` in formato ISO `AAAA-MM-GG` |
| DESCRIZIONE SPESA | `description` |
| IMPORTO | `amount_cents` |
| PERCENTUALE MANU | `manu_percentage` |
| PERCENTUALE LEO | `leo_percentage` |
| IMPORTO DOVUTO MANU | calcolato automaticamente |
| IMPORTO DOVUTO LEO | calcolato automaticamente |
| IMPORTO PAGATO MANU | `paid_manu_cents` |
| IMPORTO PAGATO LEO | `paid_leo_cents` |

Gli importi sono memorizzati in centesimi interi, evitando errori binari dei numeri decimali. La quota Manu viene arrotondata al centesimo più vicino; la quota Leo riceve il residuo, quindi la somma delle due quote coincide sempre con l'importo totale.

## Apertura e compilazione

Prerequisiti consigliati:

- Android Studio Ladybug o successivo;
- JDK 17;
- Android SDK 35.

Passaggi:

1. Estrarre l'archivio.
2. Aprire la cartella `SpeseAndroid` in Android Studio.
3. Attendere il completamento di Gradle Sync.
4. Avviare l'app su un emulatore o dispositivo Android 7.0 (API 24) o successivo.

Da terminale:

```bash
./gradlew test
./gradlew assembleDebug
```

L'APK di debug viene generato in `app/build/outputs/apk/debug/app-debug.apk`.

## Struttura principale

- `MainActivity.kt`: gestione della schermata, del modulo e delle azioni CRUD;
- `ExpenseDbHelper.kt`: database SQLite locale e precaricamento iniziale;
- `ExpenseAdapter.kt`: elenco delle spese;
- `ExpenseCalculator.kt`: calcolo delle quote;
- `MoneyUtils.kt`: conversione e formattazione degli importi;
- `app/src/test`: test unitari per ripartizioni e importi.

## Note sulla persistenza

I dati restano esclusivamente sul dispositivo nel database privato dell'app (`spese.db`). La disinstallazione dell'app o la cancellazione dei dati dell'app elimina il database. Non sono richiesti account, rete o permessi speciali.
