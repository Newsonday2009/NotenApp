# NotenApp Android

Diese Android-Version ist für Smartphone und Tablet gedacht.

## Bereits eingebaut
- Navigation: Noten | Home | Finanzen
- Klassenstufen 5 bis 13
- Eigene Fächer pro Klassenstufe
- Fächer hinzufügen, öffnen und löschen
- Fachseite mit Bereichen für Klausuren/Klassenarbeiten und mündliche Noten
- Finanzbereich als Platzhalter

Die Daten werden aktuell noch nicht dauerhaft gespeichert. Eine Datenbank folgt später.

## APK über GitHub bauen
Bei Änderungen an der Android-App startet GitHub Actions automatisch den Workflow `Android APK`.
Nach erfolgreichem Lauf kann die Datei `NotenApp-debug-apk` unter den Artifacts des Workflow-Laufs heruntergeladen werden. Darin liegt `app-debug.apk`.
