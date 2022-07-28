# Applicazioni Internet 2017-18

## Esercitazione 3

##### Gruppo 3

* **Alberto Tambone**
* **Chiara Salerno**
* **Gianluca Giavatto**
* **Simone Riggi**

Sviluppare un’applicazione back-end, basata sull’utilizzo del framework Spring Boot, che offra un insieme di
punti di accesso REST volti a permettere il salvataggio e l’accesso a sequenze di posizioni geografiche di
singoli utenti come fatto nella prima esercitazione, rispettando quindi gli stessi requisiti.

1. Si gestisca l’autenticazione degli utenti tramite l’utilizzo di Spring Security ed il protocollo OAuth2
configurandone opportunamente gli endpoint necessari a richiedere un token d’accesso basato su
JWT (JSON Web Token).

2. Definire tre ruoli diversi per il controllo d’accesso:
- ADMIN
- USER
- CUSTOMER

Gli utenti con ruolo ADMIN possono accedere a tutti i dati memorizzati dagli USER e dai
CUSTOMER. Gli utenti con ruolo USER possono salvare e successivamente recuperare i propri dati.
Gli utenti con ruolo CUSTOMER possono recuperare il numero di posizioni presenti in un area
delimitata da un poligono in un dato intervallo temporale, e successivamente decidere se
acquistare o meno le posizioni registrandone la transazione; qualora questo avvenga sarà
necessario attribuire agli utenti che hanno conferito i singoli dati una frazione del prezzo
corrisposto proporzionale alla quantità di dati effettivamente acquistata (nell’ambito
dell’esercitazione, la transazione è simulata e non avviene nessuno scambio di denaro)

3. Utilizzare MongoDB per salvare i dati ed effettuare le query geografiche atte a stabilire le posizioni
appartenenti ad una data area. (i dettagli di tale argomento saranno oggetto delle prossime lezioni)

4. Pacchettizzare l’applicazione utilizzando maven e creare un container docker che la ospiti.