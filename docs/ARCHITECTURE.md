# BetterRTP-Architektur

## Komponenten

`BetterRTP` ist der Composition Root. Die Plugin-Klasse erstellt langlebige
Komponenten, verbindet ihre Abhängigkeiten und steuert Laden, Reload und
Herunterfahren. Neue Kernlogik soll nicht über `BetterRTP.getInstance()` nach
Abhängigkeiten suchen, sondern diese im Konstruktor erhalten.

Der RTP-Ablauf ist in folgende Verantwortlichkeiten getrennt:

- `RTP` hält Konfiguration und aktive Sitzungen.
- `RTPPlayer` ist die Zustandsmaschine einer einzelnen Anfrage.
- `RtpCandidateFinder` erzeugt und validiert Kandidaten.
- `RTPTeleport` führt den asynchronen Teleport und dessen Abschluss aus.
- `RTPTransaction` garantiert genau einen Economy-Abschluss oder Rollback.
- `RTPSessionManager` verhindert parallele Sitzungen desselben Spielers.
- `QueueService` enthält die testbare Auswahl- und Persistenzlogik.
- `QueueGenerator` koordiniert die Scheduler-Schritte der Vorberechnung.

Allgemeine Einstellungen werden als unveränderlicher `SettingsSnapshot`
geladen. RTP-spezifische Werte liegen in `RtpRuntimeSettings`. Ein Reload
veröffentlicht einen neuen Snapshot erst, nachdem er vollständig gelesen und
validiert wurde.

## Scheduler-Vertrag

Alle Scheduler-Wechsel laufen über `AsyncHandler`.

- Entity Scheduler: Spieler- und Entity-Zustand, Inventare und Nachrichten an
  Spieler.
- Region Scheduler: Welt-, Block- und Chunk-Zugriffe an einer Position.
- Global Region Scheduler: serverweiter Zustand und Lifecycle-Operationen.
- Async Scheduler: Datenbank, Dateien, HTTP und reine Berechnungen ohne Zugriff
  auf lebende Bukkit-Objekte.

Asynchrone Arbeit darf keine veränderlichen `Location`- oder Entity-Zustände
mitnehmen. Dafür werden unveränderliche Snapshots wie `QueuePosition`,
`QueueRange`, `RtpRequest` und `RtpWorldSnapshot` verwendet.

## Kompatibilitätsregeln

- Öffentliche Konstruktoren und statische Queue-Hilfen bleiben innerhalb der
  4.x-Serie erhalten, auch wenn intern injizierte Alternativen verwendet
  werden.
- Bestehende Konfigurationspfade werden migriert oder weiterhin gelesen.
- Das Hauptpackage wird innerhalb der 4.x-Serie nicht umbenannt.
- Veraltete APIs werden mit einer Alternative dokumentiert und frühestens in
  einer neuen Hauptversion entfernt.

## Fehlerverhalten

Fehler externer Schutz-Plugins führen zu einem sicheren Abbruch der
Positionsprüfung. Datenbank-, Konfigurations- und Schedulerfehler werden mit
Kontext protokolliert. Erwartbare fachliche Abbrüche werden als Ergebnisse
behandelt und nicht über `NullPointerException` gesteuert.
