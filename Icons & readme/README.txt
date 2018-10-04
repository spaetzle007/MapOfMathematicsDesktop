README

Funktionalität
-Programm zur Darstellung der Mathematik für Physiker und weitergehender Themen am KIT
-"MOM.txt" enthält codierte Einträge und wird täglich synchronisiert

----------------------------------Benutzung von MOM---------------------------------------------------------------------
MOM
-Ansichtprogramm: Nur lesender Zugriff auf Daten
-Internetzugang einmal täglich zur Synchronisierung notwendig

Bedienungsanleitung
-Baumstruktur der Einträge mit Farbcodierung: 
 rot=übergeordnet; grün=gleiche Ebene; gelb=untergeordnet
 blau=Querverbindung
-Suchfunktion(violette Einträge)
-"Zurück"-Button öffnet übergeordneten Eintrag


------------------------------Benutzung von MOMEdit----------------------------------------------------------------------
MOMEdit
-Editierprogramm: Lesender und schreibender Zugriff
-Ständiger Internetzugang erforderlich - Nach Programmende wird automatisch synchronisiert
-Manuelles Synchronisieren möglich
-Nur eine Person darf gleichzeitig edietieren
 ->Sicherstellen, dass es keine verschiedenen "MOM.txt"-Versionen gibt

Ansichtsmenü
-Beim Programmstart wird Ansichtsmenü mit Ansicht über alle Einträge geöffnet
-Erster Button-Block
 -Von hier aus können neue Einträge erstellt und bestehende Einträge bearbeitet werden(obere 4 Buttons)
 -Zu bearbeitende Einträge können per Doppelklick ausgewählt werden
 -Falls man Such-Funktion benutzt hat, kann Anfangsansicht mit "Alle anzeigen" hergestellt werden
-Zweiter Button-Block
 -"Links aktualisieren": Berechnet aufgrund der übergeordneten Links die gleich geordneten und 
  untergeordneten Links für jeden Eintrag neu. Normalerweise nicht benötigt.
 -"Sicherungskopie": Erstellt unter einem bestimmten Pfad des Entwicklers eine Sicherungskopie. Nicht benötigt.
 -"Dbx-Synchro": Vorzeitige Dropbox-Synchronisierung durchführen. Normalerweise nicht benötigt.
 
Editiermenü
-Eingabe von Titel, Text und Links des ausgewählten Eintrags
-Mit Buttons speichern, Testansicht und zurückkehren zum Ansichtsmenü möglich
 -"Speichern"-Button speichert nur programmintern, Upload bzw Ändern von "MOM.txt" zusätzlich nötig
-Spezifische Informationen zum Editieren weiter unten

Programmende
-Nach Programmende werden die Änderungen in "MOM.txt" gespeichert und mit Dropbox synchronisiert
-Wenn keine Internetverbindung vorhanden ist, werden keine Daten gespeichert - Auch nicht lokal in "MOM.txt"

-----------------------------Tipps fürs Erstellen und Bearbeiten von Einträgen------------------------------------------
GuideLines für Einträge:
-Eindeutige Titel verwenden
-Zeilenumbrüche so setzen, dass kompletter Text im Bild ist
-Überschriften mit "\textbf{überschrift}" für jeden Textabschnitt
-Aufzählungen mit "-", "1.", "a)"
-In bestehenden Einträgen nur Schreibfehler ändern - nichts vorhandenes löschen!

Tipps zum Erstellen neuer Einträge
-Notwendig, dass jeder Eintrag von Anfang an einen Titel hat
-Verwendung von LaTeX:
 -Tatsächlich: Die ganze Zeit im MatheModus
 -Vor jeder Zeile wird "\text{....}" ergänzt->Befinde mich die ganze Zeit im TextModus
 ->Formeln setzen mit "$....$"
-Einstellen der Verknüpfungen
 -Auswählen des gewünschten Eintrags mit dem Cursor
 -"SPACE"->Als übergeordneter Eintrag speichern
 -"ENTER"->Als Querverbindung speichern