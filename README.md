# NotenApp

Erste Version einer JavaFX-App zur Schul- und Notenverwaltung.

## Aktueller Stand

- Navigation zwischen **Noten**, **Home** und **Finanzen**
- Auswahl der Klassenstufe von 5 bis 13
- Für jede Klassenstufe eigene Fächerliste
- Fächer können hinzugefügt und gelöscht werden
- Fächer können geöffnet werden
- In einem Fach gibt es bereits getrennte Bereiche für **Klassenarbeiten/Klausuren** und **mündliche Noten**
- Finanzbereich ist noch ein Platzhalter

Die Daten werden im Moment nur während der Laufzeit gespeichert. Eine Datenbank kommt später dazu.

## Starten

Voraussetzung: Java 17 oder neuer und Maven.

```bash
mvn javafx:run
```

Alternativ kann das Projekt als Maven-Projekt in IntelliJ IDEA geöffnet werden.
