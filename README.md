# BetterRTP 4

[![Build](https://github.com/GAMINGGILDE/BetterRTP/actions/workflows/run-tests.yaml/badge.svg)](https://github.com/GAMINGGILDE/BetterRTP/actions/workflows/run-tests.yaml)
[![Issues](https://img.shields.io/github/issues/GAMINGGILDE/BetterRTP)](https://github.com/GAMINGGILDE/BetterRTP/issues)
[![Lizenz](https://img.shields.io/github/license/GAMINGGILDE/BetterRTP)](LICENSE)

BetterRTP ist ein umfangreich konfigurierbares Zufallsteleport-Plugin für moderne Paper- und Folia-Server. Dieses Repository ist der von GAMINGGILDE gepflegte Fork. Version 4 konzentriert sich ausschließlich auf aktuelle Serverplattformen und regionssicheres Scheduling.

## Funktionen

- Zufällige Teleportation mit konfigurierbaren Mittelpunkten, Formen sowie minimalen und maximalen Radien
- Weltabhängige Einstellungen, Weltüberschreibungen und Berechtigungsgruppen
- Prüfung sicherer Zielorte anhand von Blöcken, Biomen, Höhengrenzen und Nether-Bedingungen
- Optionale Abklingzeiten, Verzögerungen, Economy-Kosten und Nahrungskosten
- Vorab generierte Teleportziele mit SQLite-Speicherung
- Folia-kompatibles Scheduling für Regionen, Zielorte und Entitäten
- Schlanke optionale Integrationen mit Vault, PlaceholderAPI, WorldGuard, GriefPrevention, Towny und Lands
- Mitgelieferte Sprachdateien unter [`src/main/resources/lang`](src/main/resources/lang)

## Voraussetzungen

Für den Betrieb werden Paper oder Folia ab Version 26.1 und Java 25 benötigt. Spigot und ältere Minecraft-Versionen werden ab BetterRTP 4 nicht mehr unterstützt.

Zum Bauen von BetterRTP werden benötigt:

- Git
- JDK 25
- Apache Maven 3.9 oder neuer, verfügbar über den Befehl `mvn`

Das Projekt wird mit Java 25 gebaut und erzeugt Java-25-Bytecode.

## Plugin bauen

Klone diesen Fork und führe Maven im Stammverzeichnis des Repositorys aus:

```bash
git clone https://github.com/GAMINGGILDE/BetterRTP.git
cd BetterRTP
mvn clean package
```

Das fertige, einschließlich seiner benötigten Bibliotheken gepackte Server-Plugin wird hier erzeugt:

```text
target/BetterRTP-4.0.0-SNAPSHOT.jar
```

`mvn test` kompiliert das Projekt und führt die automatisierten Unit-Tests aus. Diese prüfen unter anderem die Kreis- und Quadratberechnung sowie die Erzeugung gültiger RTP-Koordinaten.

## Veröffentlichungen

Versionierte Builds stehen unter [GitHub Releases](https://github.com/GAMINGGILDE/BetterRTP/releases) bereit. Ein Git-Tag im Format `v3.7`, `v4.0.0` oder `v4.0.0-rc.1` startet automatisch einen reproduzierbaren Release-Build. Das GitHub Release enthält die fertige Plugin-JAR und eine Datei mit ihrer SHA-256-Prüfsumme.

Die Version im Tag ohne das führende `v` muss exakt mit der Version in der `pom.xml` übereinstimmen.

### Entwicklungsprofil

Das optionale Profil `dev` schreibt die erzeugte Core-JAR in das lokale Plugin-Verzeichnis, das in der `pom.xml` konfiguriert ist:

```bash
mvn clean package -Pdev
```

Dieser Ausgabepfad ist auf die jeweilige Entwicklungsumgebung zugeschnitten. In CI-Systemen und auf anderen Rechnern sollte der normale Build-Befehl verwendet werden.

### Weitere Projekte

Der Build im Stammverzeichnis erzeugt ausschließlich das BetterRTP-Core-Plugin. `BetterRTPAddons` und `RTPEachWorld` sind ältere, eigenständige Projekte mit jeweils eigener Maven-Konfiguration und eigenen Kompatibilitätsanforderungen. Sie müssen bei Bedarf aus ihrem jeweiligen Verzeichnis gebaut werden und sind nicht Teil des Maven-Reaktors im Stammverzeichnis.

## Installation

1. Stoppe den Minecraft-Server vollständig.
2. Kopiere `target/BetterRTP-4.0.0-SNAPSHOT.jar` in das Verzeichnis `plugins` des Servers.
3. Entferne oder archiviere ältere BetterRTP-JARs, sodass nur eine Version geladen wird.
4. Starte den Server und prüfe die erzeugte Konfiguration unter `plugins/BetterRTP`.
5. Teste die Teleportation mit `/rtp`. Nach reinen Konfigurationsänderungen kann `/rtp reload` verwendet werden.

Bei Plugin-Updates – insbesondere bei Änderungen am Scheduler oder an der Folia-Unterstützung – sollte der Server vollständig neu gestartet werden. Plugin-Reloader werden nicht empfohlen.

## Konfiguration und Übersetzungen

Die Standardkonfiguration ist in [`src/main/resources/config.yml`](src/main/resources/config.yml) dokumentiert. Die Sprachdateien befinden sich unter [`src/main/resources/lang`](src/main/resources/lang).

Alle strukturellen Konfigurationsdateien besitzen ab BetterRTP 4 den Eintrag `Config-Version: 4`. Beim ersten Start mit vorhandenen Dateien aus Version 3.7 werden diese automatisch migriert. Vor jeder Änderung legt BetterRTP eine Sicherung wie `config.yml.v3.bak` an. Bereits vorhandene Einstellungen bleiben erhalten; neue Standardwerte werden nur zur Laufzeit ergänzt und nicht stillschweigend in die Benutzerdateien geschrieben.

Weltbezogene Einstellungen verwenden in Version 4 normale YAML-Mappings statt Listen mit jeweils einem Eintrag:

```yaml
CustomWorlds:
  survival:
    MaxRadius: 5000
    MinRadius: 100

Overrides:
  lobby: survival
```

Dasselbe vereinfachte Format gilt für `WorldType`, `PermissionGroup.Groups`, `Locations` und `CustomWorlds.Prices`. Veraltete Materialnamen aus 3.7 werden bei der Migration in aktuelle Paper-Materialnamen übersetzt. Ungültige Radien, Höhen, Formen, Welttypen, Preise und Materialien werden beim Start mit dem genauen Dateinamen und Konfigurationspfad gemeldet.

Die SQLite-Datenbank unter `plugins/BetterRTP/data/database.db` besitzt ebenfalls eine Schema-Version. Vor der ersten Schema-Migration wird eine Sicherung wie `database.db.schema-v0.bak` erstellt. Konfigurations- und Datenbank-Sicherungen sollten erst gelöscht werden, nachdem die migrierte Installation erfolgreich getestet wurde.

Neue und aktualisierte Übersetzungen sollten dieselben Schlüssel wie `en.yml` verwenden. Bestehende Schlüssel dürfen nur umbenannt oder entfernt werden, wenn gleichzeitig ihre Verwendung im Java-Code angepasst wird.

## Mitwirken

Fehlerberichte und Pull Requests sind über den [Issue-Tracker von GAMINGGILDE](https://github.com/GAMINGGILDE/BetterRTP/issues) willkommen. Ein Bericht zu einem Laufzeitfehler sollte folgende Angaben enthalten:

- den vollständigen Stacktrace
- Serversoftware und exakte Build-Version
- verwendete Java-Version
- BetterRTP-Version
- relevante Konfiguration und installierte Integrations-Plugins

Führe vor dem Erstellen eines Pull Requests folgenden Befehl aus:

```bash
mvn clean package
```

## Danksagung

BetterRTP wurde ursprünglich von [SuperRonanCraft](https://github.com/SuperRonanCraft) entwickelt und gepflegt. Dieser Fork wird von [Christian F](https://github.com/CFPlusPlus) für [GAMINGGILDE](https://github.com/GAMINGGILDE) betreut.

Das Plugin verwendet direkt die Scheduler- und Teleport-APIs von Paper und Folia. [ParticleLib](https://github.com/ByteZ1337/ParticleLib) wird für Partikeleffekte in die Plugin-JAR eingebettet. Die unterstützten optionalen Integrationen sind in der `pom.xml` aufgeführt.

Die ursprüngliche BetterRTP-Ressource ist weiterhin auf [SpigotMC](https://www.spigotmc.org/resources/36081/) verfügbar.

## Lizenz

BetterRTP wird unter der [MIT-Lizenz](LICENSE) veröffentlicht.
