# Release-Checkliste

## Vorbereitung

- [ ] Projektversion in `pom.xml` enthält kein `SNAPSHOT`.
- [ ] Upgrade- und Konfigurationshinweise sind aktuell.
- [ ] Testserver besitzt eine Sicherung von Konfiguration und SQLite-Datenbank.
- [ ] Paper- und Folia-Zielversionen in Maven und CI stimmen überein.

## Automatische Prüfungen

- [ ] `./mvnw clean verify` ist erfolgreich.
- [ ] Paper-Smoke-Test startet BetterRTP, führt Diagnosebefehle aus und füllt
      die Queue ohne schwere Fehler.
- [ ] Folia-Smoke-Test erfüllt dieselben Prüfungen ohne Ownership- oder
      Schedulerfehler.
- [ ] Die Release-JAR und ihre SHA-256-Datei werden erzeugt.

## Manueller Testserver

- [ ] RTP in Overworld, Nether und End.
- [ ] Verzögerung sowie Abbruch durch Bewegung, Schaden und Logout.
- [ ] Gleichzeitige RTP-Anfragen desselben Spielers.
- [ ] `/rtp reload` während einer aktiven RTP-Anfrage.
- [ ] Queue-Erzeugung, Entnahme und erneutes Auffüllen.
- [ ] GriefPrevention sowie alle für den Live-Server relevanten Integrationen.
- [ ] Economy-Kosten, Nahrungskosten, Cooldown und Bypass-Rechte.
- [ ] Erster Beitritt, Respawn und Welt-Overrides.
- [ ] Konsole und Spieler können Diagnose- und Edit-Befehle ohne Fehler nutzen.

## Veröffentlichung

- [ ] Release-Commit ist sauber und vollständig gepusht.
- [ ] Signierter oder geschützter Tag `v<Version>` wurde erstellt.
- [ ] GitHub Actions ist einschließlich beider Server-Smoke-Tests erfolgreich.
- [ ] Download der veröffentlichten JAR stimmt mit der SHA-256-Datei überein.
- [ ] Live-Rollout erfolgt mit beobachteten Logs und vorbereitetem Rollback.
