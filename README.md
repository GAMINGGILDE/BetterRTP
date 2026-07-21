# BetterRTP

[![Build](https://github.com/GAMINGGILDE/BetterRTP/actions/workflows/run-tests.yaml/badge.svg)](https://github.com/GAMINGGILDE/BetterRTP/actions/workflows/run-tests.yaml)
[![Issues](https://img.shields.io/github/issues/GAMINGGILDE/BetterRTP)](https://github.com/GAMINGGILDE/BetterRTP/issues)
[![Lizenz](https://img.shields.io/github/license/GAMINGGILDE/BetterRTP)](LICENSE)

BetterRTP ist ein umfangreich konfigurierbares Zufallsteleport-Plugin für Minecraft-Server. Dieses Repository ist der von GAMINGGILDE gepflegte Fork. Der Schwerpunkt liegt auf der Kompatibilität mit aktuellen Serverversionen, einschließlich regionssicherem Scheduling und Teleportieren unter Folia 26.1.x.

## Funktionen

- Zufällige Teleportation mit konfigurierbaren Mittelpunkten, Formen sowie minimalen und maximalen Radien
- Weltabhängige Einstellungen, Weltüberschreibungen und Berechtigungsgruppen
- Prüfung sicherer Zielorte anhand von Blöcken, Biomen, Höhengrenzen und Nether-Bedingungen
- Optionale Abklingzeiten, Verzögerungen, Economy-Kosten und Nahrungskosten
- Vorab generierte Teleportziele mit SQLite-Speicherung
- Folia-kompatibles Scheduling für Regionen, Zielorte und Entitäten
- Optionale Integrationen mit Vault, WorldGuard, GriefPrevention, Towny, Lands, Residence, PlaceholderAPI und weiteren Schutz-Plugins
- Mitgelieferte Sprachdateien unter [`src/main/resources/lang`](src/main/resources/lang)

## Voraussetzungen

Für den Betrieb wird ein kompatibler Spigot-, Paper- oder Folia-Server benötigt. Verwende die Java-Version, die von deiner jeweiligen Serversoftware vorausgesetzt wird. Folia 26.1.x wird mit Java 25 betrieben.

Zum Bauen von BetterRTP werden benötigt:

- Git
- JDK 17 oder neuer; JDK 25 wurde lokal erfolgreich getestet
- Apache Maven 3.9 oder neuer, verfügbar über den Befehl `mvn`

Das Projekt erzeugt weiterhin Java-8-Bytecode, um ältere Serverversionen möglichst lange zu unterstützen. Build- und Laufzeitumgebung sind daher getrennt zu betrachten: Für den Minecraft-Server muss immer die Java-Version verwendet werden, die dessen Distribution verlangt.

## Plugin bauen

Klone diesen Fork und führe Maven im Stammverzeichnis des Repositorys aus:

```bash
git clone https://github.com/GAMINGGILDE/BetterRTP.git
cd BetterRTP
mvn clean package
```

Das fertige, einschließlich seiner benötigten Bibliotheken gepackte Server-Plugin wird hier erzeugt:

```text
target/BetterRTP-3.7.jar
```

`mvn test` prüft derzeit die Kompilierung und den Build. Das Projekt enthält aktuell noch keine automatisierten Unit- oder Integrationstests.

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
2. Kopiere `target/BetterRTP-3.7.jar` in das Verzeichnis `plugins` des Servers.
3. Entferne oder archiviere ältere BetterRTP-JARs, sodass nur eine Version geladen wird.
4. Starte den Server und prüfe die erzeugte Konfiguration unter `plugins/BetterRTP`.
5. Teste die Teleportation mit `/rtp`. Nach reinen Konfigurationsänderungen kann `/rtp reload` verwendet werden.

Bei Plugin-Updates – insbesondere bei Änderungen am Scheduler oder an der Folia-Unterstützung – sollte der Server vollständig neu gestartet werden. Plugin-Reloader werden nicht empfohlen.

## Konfiguration und Übersetzungen

Die Standardkonfiguration ist in [`src/main/resources/config.yml`](src/main/resources/config.yml) dokumentiert. Die Sprachdateien befinden sich unter [`src/main/resources/lang`](src/main/resources/lang).

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

Das Projekt bindet unter anderem [PaperLib](https://github.com/PaperMC/PaperLib), [FoliaLib](https://github.com/TechnicallyCoded/FoliaLib) und [ParticleLib](https://github.com/ByteZ1337/ParticleLib) ein. Weitere optionale Server-Integrationen sind in der `pom.xml` aufgeführt.

Die ursprüngliche BetterRTP-Ressource ist weiterhin auf [SpigotMC](https://www.spigotmc.org/resources/36081/) verfügbar.

## Lizenz

BetterRTP wird unter der [MIT-Lizenz](LICENSE) veröffentlicht.
