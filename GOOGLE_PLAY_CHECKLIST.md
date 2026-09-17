# Google-Play-Checkliste – NotenApp

Stand: 17.09.2026

## Bereits technisch umgesetzt

- `targetSdk 36` / Android 16 für neue Apps und Updates ab 31.08.2026.
- `compileSdk 36`.
- Keine unnötigen Android-Berechtigungen im Manifest.
- Keine Internetberechtigung in der aktuellen Version.
- Unverschlüsselter HTTP-Verkehr ist deaktiviert (`usesCleartextTraffic=false`).
- Network Security Config erlaubt nur System-Zertifikate.
- Android-Backups sind deaktiviert (`allowBackup=false`).
- Nur die Launcher-Activity ist exportiert; sie muss für den App-Start exportiert sein.
- Release-Build nutzt R8/Minifizierung und entfernt ungenutzte Ressourcen.
- Keine Werbe-, Analyse- oder Tracking-SDKs eingebaut.
- Datenschutzinformationen sind in der App erreichbar.
- Entwurf einer öffentlichen Datenschutzerklärung liegt unter `docs/privacy-policy.html`.

## Vor einer Google-Play-Veröffentlichung noch nötig

1. Datenschutzerklärung öffentlich hosten, z. B. über GitHub Pages, und die URL in der Play Console eintragen.
2. Das Formular **Datensicherheit** in der Play Console exakt entsprechend der tatsächlich veröffentlichten App ausfüllen.
3. Einen **Release App Bundle (.aab)** erstellen und über **Play App Signing** signieren/veröffentlichen. Keystore-Dateien und Passwörter niemals ins Repository committen.
4. Store-Eintrag, Inhaltsbewertung, Zielgruppe und ggf. Werbeangaben ausfüllen.
5. Vor jedem neuen SDK prüfen, welche Daten es erhebt oder weitergibt, und Datenschutzangaben aktualisieren.
6. Falls später Benutzerkonten eingebaut werden: In-App- und Web-Möglichkeit zur Kontolöschung bereitstellen.
7. Falls später Cloud-Synchronisierung oder Serverzugriff hinzukommt: ausschließlich HTTPS/TLS verwenden und die Datenschutzerklärung sowie Datensicherheitsangaben aktualisieren.
8. Falls Noten oder Finanzdaten dauerhaft gespeichert werden: lokale Speicherung im privaten App-Speicher und für besonders sensible Daten zusätzlich Verschlüsselung vorsehen.

## Wichtig

Diese Datei ist eine technische Checkliste, keine Garantie für die Freigabe durch Google Play. Google-Play-Richtlinien können sich ändern und müssen vor einer tatsächlichen Veröffentlichung erneut geprüft werden.
