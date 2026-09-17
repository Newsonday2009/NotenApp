package de.notenapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int COLOR_BACKGROUND = Color.rgb(244, 247, 252);
    private static final int COLOR_PRIMARY = Color.rgb(58, 91, 192);
    private static final int COLOR_PRIMARY_DARK = Color.rgb(40, 67, 150);
    private static final int COLOR_ACCENT = Color.rgb(103, 80, 164);
    private static final int COLOR_GREEN = Color.rgb(34, 133, 88);
    private static final int COLOR_TEXT = Color.rgb(31, 36, 48);
    private static final int COLOR_MUTED = Color.rgb(102, 111, 132);
    private static final int COLOR_BORDER = Color.rgb(222, 228, 239);
    private static final int COLOR_DANGER = Color.rgb(177, 45, 45);

    private FrameLayout contentContainer;
    private int selectedClass = 12;

    // Normale Fächer-/Notenverwaltung
    private final Map<Integer, ArrayList<String>> subjectsByClass = new HashMap<>();
    private final Map<String, ArrayList<Integer>> writtenGradesBySubject = new HashMap<>();
    private final Map<String, ArrayList<Integer>> oralGradesBySubject = new HashMap<>();

    // Oberstufe Sachsen-Anhalt: vier Kurshalbjahre + Abiturprüfung
    private final ArrayList<String> upperSubjects = new ArrayList<>();
    private final Map<String, int[]> upperHalfyearPoints = new HashMap<>();
    private final Map<String, boolean[]> upperIncluded = new HashMap<>();

    private final String[] examSubjects = {"", "", "", "", ""};
    private final int[] examPoints = {-1, -1, -1, -1, -1};
    private boolean doubleWeightP1P2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(COLOR_BACKGROUND);

        contentContainer = new FrameLayout(this);
        root.addView(contentContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        root.addView(createBottomNavigation());
        setContentView(root);

        showHomePage();
    }

    // --------------------------------------------------
    // NAVIGATION
    // --------------------------------------------------

    private View createBottomNavigation() {
        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER);
        navigation.setPadding(dp(10), dp(10), dp(10), dp(12));
        navigation.setBackgroundColor(Color.WHITE);
        navigation.setElevation(dp(10));

        Button gradesButton = createNavButton("▣  Noten");
        Button homeButton = createNavButton("⌂  Home");
        Button financeButton = createNavButton("€  Finanzen");

        gradesButton.setOnClickListener(v -> showGradesPage());
        homeButton.setOnClickListener(v -> showHomePage());
        financeButton.setOnClickListener(v -> showFinancePage());

        navigation.addView(gradesButton, navParams());
        navigation.addView(homeButton, navParams());
        navigation.addView(financeButton, navParams());

        return navigation;
    }

    private LinearLayout.LayoutParams navParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(50), 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private Button createNavButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(COLOR_TEXT);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(createRoundedBackground(Color.rgb(239, 242, 248), dp(16), 0, Color.TRANSPARENT));
        return button;
    }

    // --------------------------------------------------
    // STARTSEITE
    // --------------------------------------------------

    private void showHomePage() {
        syncUpperSubjectsFromClasses();

        LinearLayout page = createVerticalPage();

        TextView eyebrow = createText("NOTENAPP", 12, true);
        eyebrow.setTextColor(COLOR_PRIMARY);
        eyebrow.setLetterSpacing(0.16f);
        page.addView(eyebrow);

        page.addView(createTitle("Dein Schul-Dashboard"));
        page.addView(createSubtitle("Noten, Oberstufe und später auch Finanzen an einem Ort."));

        LinearLayout hero = createTintedCard(Color.rgb(233, 238, 255), COLOR_PRIMARY);
        hero.addView(createText("Klasse " + selectedClass, 27, true));
        hero.addView(createText(
                getSubjectsForClass(selectedClass).size() + " Fächer angelegt",
                15,
                false
        ));

        Button openGrades = createPrimaryButton("Noten öffnen  →");
        openGrades.setOnClickListener(v -> showGradesPage());
        hero.addView(openGrades);
        page.addView(hero);

        if (selectedClass >= 11) {
            LinearLayout upperCard = createTintedCard(Color.rgb(242, 236, 255), COLOR_ACCENT);
            upperCard.addView(createText("🎓 Oberstufe Sachsen-Anhalt", 20, true));
            upperCard.addView(createText(
                    "4 Kurshalbjahre, Abiturprüfungen und Gesamtqualifikation.",
                    15,
                    false
            ));

            UpperSummary summary = calculateUpperSummary();
            if (summary.filledHalfyears > 0) {
                upperCard.addView(createText(
                        "Aktuell erfasst: " + summary.filledHalfyears + " Kurshalbjahresergebnisse",
                        14,
                        true
                ));
            }

            Button upperButton = createAccentButton("Oberstufe öffnen  →");
            upperButton.setOnClickListener(v -> showUpperSecondaryPage());
            upperCard.addView(upperButton);
            page.addView(upperCard);
        }

        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout financeCard = createMiniCard("€", "Finanzen", "Kommt als Nächstes");
        LinearLayout privacyCard = createMiniCard("✓", "Datenschutz", "Lokal & ohne Tracking");

        quickRow.addView(financeCard, weightedCardParams());
        quickRow.addView(privacyCard, weightedCardParams());

        financeCard.setOnClickListener(v -> showFinancePage());
        privacyCard.setOnClickListener(v -> showPrivacyPage());

        page.addView(quickRow);

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout.LayoutParams weightedCardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        return params;
    }

    private LinearLayout createMiniCard(String symbol, String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(16), dp(15), dp(16));
        card.setBackground(createRoundedBackground(Color.WHITE, dp(20), dp(1), COLOR_BORDER));
        card.setElevation(dp(2));

        TextView icon = createText(symbol, 24, true);
        icon.setTextColor(COLOR_PRIMARY);
        card.addView(icon);
        card.addView(createText(title, 16, true));

        TextView sub = createText(subtitle, 12, false);
        sub.setTextColor(COLOR_MUTED);
        card.addView(sub);

        return card;
    }

    // --------------------------------------------------
    // NORMALE NOTEN / FÄCHER
    // --------------------------------------------------

    private void showGradesPage() {
        LinearLayout page = createVerticalPage();

        page.addView(createTitle("Noten"));
        page.addView(createSubtitle("Wähle deine Klassenstufe und verwalte deine Fächer."));

        LinearLayout classCard = createCard();
        classCard.addView(createText("Klassenstufe", 15, true));

        Spinner classSpinner = new Spinner(this);
        ArrayList<Integer> classes = new ArrayList<>();
        for (int i = 5; i <= 12; i++) {
            classes.add(i);
        }
        ArrayAdapter<Integer> classAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                classes
        );
        classSpinner.setAdapter(classAdapter);
        classSpinner.setSelection(selectedClass - 5);
        classCard.addView(classSpinner);

        TextView gradingInfo = createText(
                selectedClass <= 10
                        ? "Bewertungssystem: Schulnoten 1 bis 6"
                        : "Bewertungssystem: Notenpunkte 0 bis 15",
                13,
                false
        );
        gradingInfo.setTextColor(COLOR_MUTED);
        classCard.addView(gradingInfo);

        if (selectedClass >= 11) {
            Button upperButton = createAccentButton("🎓  Oberstufe & Abitur");
            upperButton.setOnClickListener(v -> showUpperSecondaryPage());
            classCard.addView(upperButton);
        }

        page.addView(classCard);

        TextView subjectsTitle = createSectionTitle("Deine Fächer");
        page.addView(subjectsTitle);

        ArrayList<String> subjects = getSubjectsForClass(selectedClass);
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_activated_1,
                subjects
        );

        ListView subjectList = new ListView(this);
        subjectList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        subjectList.setAdapter(subjectAdapter);
        subjectList.setBackground(createRoundedBackground(Color.WHITE, dp(18), dp(1), COLOR_BORDER));
        subjectList.setDividerHeight(dp(1));
        page.addView(subjectList, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        EditText subjectInput = new EditText(this);
        subjectInput.setHint("Neues Fach, z. B. Mathematik");
        subjectInput.setFilterTouchesWhenObscured(true);
        subjectInput.setSingleLine(true);
        subjectInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        subjectInput.setBackground(createRoundedBackground(Color.WHITE, dp(14), dp(1), COLOR_BORDER));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        inputParams.setMargins(0, dp(12), 0, dp(8));
        page.addView(subjectInput, inputParams);

        Button addButton = createPrimaryButton("+  Fach hinzufügen");
        addButton.setFilterTouchesWhenObscured(true);
        page.addView(addButton);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(8), 0, 0);

        Button openButton = createSecondaryButton("Fach öffnen");
        openButton.setEnabled(false);

        Button deleteButton = createDangerButton("Löschen");
        deleteButton.setEnabled(false);
        deleteButton.setFilterTouchesWhenObscured(true);

        LinearLayout.LayoutParams actionParams1 = new LinearLayout.LayoutParams(0, dp(50), 1.2f);
        actionParams1.setMargins(0, 0, dp(5), 0);
        LinearLayout.LayoutParams actionParams2 = new LinearLayout.LayoutParams(0, dp(50), 0.8f);
        actionParams2.setMargins(dp(5), 0, 0, 0);
        actions.addView(openButton, actionParams1);
        actions.addView(deleteButton, actionParams2);
        page.addView(actions);

        final int[] selectedIndex = {-1};

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newClass = classes.get(position);
                if (newClass != selectedClass) {
                    selectedClass = newClass;
                    showGradesPage();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        addButton.setOnClickListener(v -> {
            String newSubject = subjectInput.getText().toString().trim();
            if (newSubject.isEmpty()) {
                Toast.makeText(this, "Bitte einen Fachnamen eingeben.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean exists = false;
            for (String subject : subjects) {
                if (subject.equalsIgnoreCase(newSubject)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                Toast.makeText(this, "Dieses Fach ist bereits vorhanden.", Toast.LENGTH_SHORT).show();
                return;
            }

            subjects.add(newSubject);
            if (selectedClass >= 11) {
                addUpperSubject(newSubject);
            }
            subjectAdapter.notifyDataSetChanged();
            subjectInput.setText("");
        });

        subjectList.setOnItemClickListener((parent, view, position, id) -> {
            selectedIndex[0] = position;
            openButton.setEnabled(true);
            deleteButton.setEnabled(true);
        });

        subjectList.setOnItemLongClickListener((parent, view, position, id) -> {
            showSubjectPage(subjects.get(position));
            return true;
        });

        openButton.setOnClickListener(v -> {
            if (selectedIndex[0] >= 0 && selectedIndex[0] < subjects.size()) {
                showSubjectPage(subjects.get(selectedIndex[0]));
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (selectedIndex[0] >= 0 && selectedIndex[0] < subjects.size()) {
                String deletedSubject = subjects.get(selectedIndex[0]);

                new AlertDialog.Builder(this)
                        .setTitle("Fach löschen?")
                        .setMessage(deletedSubject + " wird aus Klasse " + selectedClass + " entfernt.")
                        .setNegativeButton("Abbrechen", null)
                        .setPositiveButton("Löschen", (dialog, which) -> {
                            subjects.remove(selectedIndex[0]);
                            writtenGradesBySubject.remove(createGradeKey(selectedClass, deletedSubject));
                            oralGradesBySubject.remove(createGradeKey(selectedClass, deletedSubject));
                            removeUpperSubjectIfUnused(deletedSubject);

                            subjectAdapter.notifyDataSetChanged();
                            selectedIndex[0] = -1;
                            openButton.setEnabled(false);
                            deleteButton.setEnabled(false);
                        })
                        .show();
            }
        });

        showInContent(page);
    }

    private void showSubjectPage(String subject) {
        LinearLayout page = createVerticalPage();

        Button backButton = createBackButton("←  Zurück zu den Fächern");
        backButton.setOnClickListener(v -> showGradesPage());
        page.addView(backButton);

        page.addView(createTitle(subject));
        page.addView(createSubtitle(
                "Klasse " + selectedClass + "  •  " +
                        (selectedClass <= 10 ? "Noten 1–6" : "Notenpunkte 0–15")
        ));

        String writtenTitle = selectedClass >= 11 ? "Klausuren" : "Klassenarbeiten";
        page.addView(createGradeCard(subject, writtenTitle, true));
        page.addView(createGradeCard(subject, "Mündliche Leistungen", false));

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout createGradeCard(String subject, String title, boolean written) {
        LinearLayout card = createCard();

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = createText(title, 19, true);
        heading.addView(titleView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        ArrayList<Integer> entries = getGradeEntries(selectedClass, subject, written);
        if (!entries.isEmpty()) {
            TextView avgChip = createChip(calculateAverage(entries), written ? COLOR_PRIMARY : COLOR_ACCENT);
            heading.addView(avgChip);
        }
        card.addView(heading);

        if (entries.isEmpty()) {
            TextView empty = createText("Noch keine Einträge. Füge unten deine erste Bewertung hinzu.", 14, false);
            empty.setTextColor(COLOR_MUTED);
            card.addView(empty);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                final int index = i;

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(5), 0, dp(5));

                TextView number = createChip(String.valueOf(entries.get(i)), gradeAccent(entries.get(i)));
                row.addView(number);

                TextView valueText = createText(
                        "  " + (written ? "Schriftlicher" : "Mündlicher") + " Eintrag " + (i + 1),
                        15,
                        false
                );
                row.addView(valueText, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                Button removeButton = createSmallDangerButton("×");
                removeButton.setContentDescription("Eintrag löschen");
                removeButton.setFilterTouchesWhenObscured(true);
                removeButton.setOnClickListener(v -> {
                    entries.remove(index);
                    showSubjectPage(subject);
                });
                row.addView(removeButton, new LinearLayout.LayoutParams(dp(44), dp(44)));

                card.addView(row);
            }
        }

        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        addRow.setGravity(Gravity.CENTER_VERTICAL);
        addRow.setPadding(0, dp(10), 0, 0);

        Spinner gradeSpinner = new Spinner(this);
        ArrayList<Integer> possibleValues = getPossibleGradeValues();
        ArrayList<String> displayValues = new ArrayList<>();
        for (Integer value : possibleValues) {
            displayValues.add(formatGradeValue(value));
        }

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                displayValues
        );
        gradeSpinner.setAdapter(gradeAdapter);

        Button addGradeButton = createPrimaryButton(selectedClass <= 10 ? "+ Note" : "+ Punkte");
        addGradeButton.setFilterTouchesWhenObscured(true);
        addGradeButton.setOnClickListener(v -> {
            int position = gradeSpinner.getSelectedItemPosition();
            if (position >= 0 && position < possibleValues.size()) {
                entries.add(possibleValues.get(position));
                showSubjectPage(subject);
            }
        });

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(0, dp(52), 1f);
        spinnerParams.setMargins(0, 0, dp(6), 0);
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(0, dp(52), 0.85f);
        addParams.setMargins(dp(6), 0, 0, 0);

        addRow.addView(gradeSpinner, spinnerParams);
        addRow.addView(addGradeButton, addParams);
        card.addView(addRow);

        return card;
    }

    // --------------------------------------------------
    // OBERSTUFE SACHSEN-ANHALT
    // --------------------------------------------------

    private void showUpperSecondaryPage() {
        syncUpperSubjectsFromClasses();

        LinearLayout page = createVerticalPage();

        Button backButton = createBackButton("←  Zurück zu Noten");
        backButton.setOnClickListener(v -> showGradesPage());
        page.addView(backButton);

        TextView eyebrow = createText("SACHSEN-ANHALT", 12, true);
        eyebrow.setTextColor(COLOR_ACCENT);
        eyebrow.setLetterSpacing(0.14f);
        page.addView(eyebrow);

        page.addView(createTitle("Oberstufe & Abitur"));
        page.addView(createSubtitle("Qualifikationsphase der Klassen 11 und 12"));

        UpperSummary summary = calculateUpperSummary();
        page.addView(createUpperSummaryCard(summary));

        TextView semestersTitle = createSectionTitle("4 Kurshalbjahre");
        page.addView(semestersTitle);

        String[] semesterNames = {"11/1", "11/2", "12/1", "12/2"};
        for (int i = 0; i < semesterNames.length; i++) {
            final int index = i;
            LinearLayout semesterCard = createCard();

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView badge = createChip(semesterNames[i], COLOR_PRIMARY);
            top.addView(badge);

            TextView semesterTitle = createText("  Kurshalbjahr " + (i + 1), 18, true);
            top.addView(semesterTitle, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Button open = createSecondaryButton("Öffnen");
            open.setOnClickListener(v -> showHalfYearPage(index));
            top.addView(open, new LinearLayout.LayoutParams(dp(92), dp(44)));
            semesterCard.addView(top);

            String preview = buildHalfYearPreview(i);
            TextView previewView = createText(preview, 14, false);
            previewView.setTextColor(COLOR_MUTED);
            semesterCard.addView(previewView);

            int filled = countFilledHalfyear(i);
            if (filled > 0) {
                TextView info = createText(
                        filled + " Ergebnisse  •  Ø " + calculateHalfyearAverage(i) + " Punkte",
                        13,
                        true
                );
                info.setTextColor(COLOR_PRIMARY_DARK);
                semesterCard.addView(info);
            }

            semesterCard.setOnClickListener(v -> showHalfYearPage(index));
            page.addView(semesterCard);
        }

        TextView examTitle = createSectionTitle("Abiturprüfungen");
        page.addView(examTitle);

        LinearLayout examCard = createTintedCard(Color.rgb(236, 247, 242), COLOR_GREEN);
        examCard.addView(createText("5 Prüfungselemente", 20, true));
        examCard.addView(createText(
                "P1–P4 schriftlich, P5 mündlich. Jede Prüfungsleistung wird in Block II vierfach gewertet.",
                14,
                false
        ));

        String examPreview = buildExamPreview();
        if (!examPreview.isEmpty()) {
            TextView examPreviewView = createText(examPreview, 14, true);
            examPreviewView.setTextColor(COLOR_GREEN);
            examCard.addView(examPreviewView);
        }

        Button examButton = createGreenButton("Abiturprüfungen eintragen  →");
        examButton.setOnClickListener(v -> showAbiturExamPage());
        examCard.addView(examButton);
        page.addView(examCard);

        TextView subjectsTitle = createSectionTitle("Oberstufenfächer");
        page.addView(subjectsTitle);

        LinearLayout subjectCard = createCard();
        subjectCard.addView(createText(
                upperSubjects.isEmpty()
                        ? "Noch keine Oberstufenfächer angelegt."
                        : String.join("  •  ", upperSubjects),
                14,
                false
        ));

        EditText upperSubjectInput = new EditText(this);
        upperSubjectInput.setHint("Weiteres Fach hinzufügen");
        upperSubjectInput.setSingleLine(true);
        upperSubjectInput.setFilterTouchesWhenObscured(true);
        upperSubjectInput.setBackground(createRoundedBackground(Color.rgb(248, 250, 254), dp(14), dp(1), COLOR_BORDER));
        upperSubjectInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        subjectCard.addView(upperSubjectInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        ));

        Button addUpperSubject = createAccentButton("+ Fach für Oberstufe");
        addUpperSubject.setOnClickListener(v -> {
            String name = upperSubjectInput.getText().toString().trim();
            if (name.isEmpty()) {
                return;
            }
            addUpperSubject(name);
            upperSubjectInput.setText("");
            showUpperSecondaryPage();
        });
        subjectCard.addView(addUpperSubject);
        page.addView(subjectCard);

        LinearLayout infoCard = createTintedCard(Color.rgb(255, 247, 232), Color.rgb(171, 111, 19));
        infoCard.addView(createText("So rechnet Sachsen-Anhalt", 18, true));
        infoCard.addView(createText(
                "Block I: 36–40 einzubringende Kurshalbjahresergebnisse; Berechnung (P/A) × 40, maximal 600 Punkte.\n\n" +
                        "Block II: fünf Abitur-Prüfungselemente, jeweils vierfach gewertet, maximal 300 Punkte.\n\n" +
                        "Gesamt: Block I + Block II = maximal 900 Punkte. Die App zeigt eine Prognose; die endgültige Einbringung wird von der Schule geprüft.",
                14,
                false
        ));
        page.addView(infoCard);

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout createUpperSummaryCard(UpperSummary summary) {
        LinearLayout card = createTintedCard(Color.rgb(232, 237, 255), COLOR_PRIMARY);

        TextView title = createText("Deine Abi-Prognose", 21, true);
        card.addView(title);

        LinearLayout statRow = new LinearLayout(this);
        statRow.setOrientation(LinearLayout.HORIZONTAL);

        statRow.addView(createStatBox(
                "BLOCK I",
                summary.blockI >= 0 ? summary.blockI + " / 600" : "–",
                COLOR_PRIMARY
        ), weightedCardParams());

        statRow.addView(createStatBox(
                "BLOCK II",
                summary.blockIIComplete ? summary.blockII + " / 300" : summary.examCount + " / 5",
                COLOR_GREEN
        ), weightedCardParams());

        card.addView(statRow);

        TextView total;
        if (summary.isCompleteForGrade()) {
            total = createText(
                    "Gesamt: " + summary.total + " / 900  •  voraussichtliche Abiturnote " + getAbiGrade(summary.total),
                    18,
                    true
            );
            total.setTextColor(COLOR_PRIMARY_DARK);
        } else {
            total = createText(
                    "Abiturnote erscheint, sobald mindestens 36 Ergebnisse für Block I ausgewählt und alle 5 Prüfungen eingetragen sind.",
                    14,
                    false
            );
            total.setTextColor(COLOR_MUTED);
        }
        card.addView(total);

        String warning = buildUpperWarning(summary);
        if (!warning.isEmpty()) {
            TextView warningView = createText(warning, 13, true);
            warningView.setTextColor(COLOR_DANGER);
            warningView.setPadding(0, dp(8), 0, 0);
            card.addView(warningView);
        }

        return card;
    }

    private LinearLayout createStatBox(String label, String value, int accent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        box.setBackground(createRoundedBackground(Color.WHITE, dp(16), 0, Color.TRANSPARENT));

        TextView labelView = createText(label, 11, true);
        labelView.setTextColor(COLOR_MUTED);
        labelView.setLetterSpacing(0.08f);
        box.addView(labelView);

        TextView valueView = createText(value, 20, true);
        valueView.setTextColor(accent);
        box.addView(valueView);

        return box;
    }

    private void showHalfYearPage(int halfyearIndex) {
        syncUpperSubjectsFromClasses();

        String[] semesterNames = {"11/1", "11/2", "12/1", "12/2"};

        LinearLayout page = createVerticalPage();

        Button backButton = createBackButton("←  Zurück zur Oberstufe");
        backButton.setOnClickListener(v -> showUpperSecondaryPage());
        page.addView(backButton);

        page.addView(createTitle("Kurshalbjahr " + semesterNames[halfyearIndex]));
        page.addView(createSubtitle(
                "Trage hier die Kurshalbjahresergebnisse ein, die auf deinem Zeugnis stehen."
        ));

        if (upperSubjects.isEmpty()) {
            LinearLayout empty = createCard();
            empty.addView(createText("Noch keine Fächer", 18, true));
            empty.addView(createText(
                    "Füge auf der Oberstufen-Seite zuerst deine Fächer hinzu.",
                    14,
                    false
            ));
            page.addView(empty);
        } else {
            for (String subject : upperSubjects) {
                LinearLayout card = createCard();

                LinearLayout heading = new LinearLayout(this);
                heading.setOrientation(LinearLayout.HORIZONTAL);
                heading.setGravity(Gravity.CENTER_VERTICAL);

                TextView subjectName = createText(subject, 18, true);
                heading.addView(subjectName, new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                ));

                int current = getHalfyearPoints(subject)[halfyearIndex];
                TextView pointsChip = createChip(current >= 0 ? current + " P" : "–", current >= 0 ? gradeAccent(current) : COLOR_MUTED);
                heading.addView(pointsChip);

                card.addView(heading);

                LinearLayout editRow = new LinearLayout(this);
                editRow.setOrientation(LinearLayout.HORIZONTAL);
                editRow.setGravity(Gravity.CENTER_VERTICAL);

                Spinner pointsSpinner = new Spinner(this);
                ArrayList<String> values = new ArrayList<>();
                values.add("Noch nicht eingetragen");
                for (int points = 15; points >= 0; points--) {
                    values.add(points + (points == 1 ? " Punkt" : " Punkte"));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        values
                );
                pointsSpinner.setAdapter(adapter);
                pointsSpinner.setSelection(current < 0 ? 0 : 16 - current);

                editRow.addView(pointsSpinner, new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1f
                ));

                card.addView(editRow);

                CheckBox include = new CheckBox(this);
                include.setText("In Block I einbringen");
                include.setTextColor(COLOR_TEXT);
                include.setChecked(getIncludedFlags(subject)[halfyearIndex]);
                include.setEnabled(current >= 0);
                card.addView(include);

                pointsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    private boolean firstCall = true;

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (firstCall) {
                            firstCall = false;
                            return;
                        }

                        if (position == 0) {
                            getHalfyearPoints(subject)[halfyearIndex] = -1;
                            getIncludedFlags(subject)[halfyearIndex] = false;
                            include.setChecked(false);
                            include.setEnabled(false);
                        } else {
                            int points = 16 - position;
                            boolean wasEmpty = getHalfyearPoints(subject)[halfyearIndex] < 0;
                            getHalfyearPoints(subject)[halfyearIndex] = points;
                            include.setEnabled(true);
                            if (wasEmpty) {
                                getIncludedFlags(subject)[halfyearIndex] = true;
                                include.setChecked(true);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                include.setOnCheckedChangeListener((buttonView, isChecked) ->
                        getIncludedFlags(subject)[halfyearIndex] = isChecked
                );

                page.addView(card);
            }
        }

        LinearLayout bottomInfo = createTintedCard(Color.rgb(239, 245, 255), COLOR_PRIMARY);
        bottomInfo.addView(createText(
                "Erfasst: " + countFilledHalfyear(halfyearIndex) + " Ergebnisse  •  Ø " +
                        (countFilledHalfyear(halfyearIndex) == 0 ? "–" : calculateHalfyearAverage(halfyearIndex) + " Punkte"),
                15,
                true
        ));
        bottomInfo.addView(createText(
                "Mit „In Block I einbringen“ legst du fest, welche Halbjahresleistung in die Abi-Prognose eingeht.",
                13,
                false
        ));
        page.addView(bottomInfo);

        showInContent(wrapInScrollView(page));
    }

    private void showAbiturExamPage() {
        syncUpperSubjectsFromClasses();

        LinearLayout page = createVerticalPage();

        Button backButton = createBackButton("←  Zurück zur Oberstufe");
        backButton.setOnClickListener(v -> showUpperSecondaryPage());
        page.addView(backButton);

        page.addView(createTitle("Abiturprüfungen"));
        page.addView(createSubtitle("Block II · fünf Prüfungselemente"));

        if (upperSubjects.isEmpty()) {
            LinearLayout warning = createTintedCard(Color.rgb(255, 247, 232), Color.rgb(171, 111, 19));
            warning.addView(createText("Lege zuerst Oberstufenfächer an.", 15, true));
            page.addView(warning);
        }

        String[] labels = {
                "P1 · schriftlich · erhöhtes Niveau",
                "P2 · schriftlich · erhöhtes Niveau",
                "P3 · schriftlich · grundlegendes Niveau",
                "P4 · schriftlich · grundlegendes Niveau",
                "P5 · mündlich"
        };

        for (int i = 0; i < 5; i++) {
            final int examIndex = i;

            LinearLayout card = createCard();
            card.addView(createText(labels[i], 17, true));

            TextView hint = createText("Prüfungsfach", 12, true);
            hint.setTextColor(COLOR_MUTED);
            card.addView(hint);

            Spinner subjectSpinner = new Spinner(this);
            ArrayList<String> subjectChoices = new ArrayList<>();
            subjectChoices.add("Fach wählen");
            subjectChoices.addAll(upperSubjects);

            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    subjectChoices
            );
            subjectSpinner.setAdapter(subjectAdapter);

            int subjectPosition = 0;
            if (!examSubjects[i].isEmpty()) {
                for (int p = 1; p < subjectChoices.size(); p++) {
                    if (subjectChoices.get(p).equalsIgnoreCase(examSubjects[i])) {
                        subjectPosition = p;
                        break;
                    }
                }
            }
            subjectSpinner.setSelection(subjectPosition);
            card.addView(subjectSpinner);

            TextView pointsLabel = createText("Prüfungsergebnis", 12, true);
            pointsLabel.setTextColor(COLOR_MUTED);
            card.addView(pointsLabel);

            Spinner pointsSpinner = new Spinner(this);
            ArrayList<String> pointChoices = new ArrayList<>();
            pointChoices.add("Punkte wählen");
            for (int points = 15; points >= 0; points--) {
                pointChoices.add(points + (points == 1 ? " Punkt" : " Punkte"));
            }

            ArrayAdapter<String> pointsAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    pointChoices
            );
            pointsSpinner.setAdapter(pointsAdapter);
            pointsSpinner.setSelection(examPoints[i] < 0 ? 0 : 16 - examPoints[i]);
            card.addView(pointsSpinner);

            subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private boolean firstCall = true;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (firstCall) {
                        firstCall = false;
                        return;
                    }
                    examSubjects[examIndex] = position == 0 ? "" : subjectChoices.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            pointsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private boolean firstCall = true;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (firstCall) {
                        firstCall = false;
                        return;
                    }
                    examPoints[examIndex] = position == 0 ? -1 : 16 - position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            page.addView(card);
        }

        LinearLayout weightingCard = createTintedCard(Color.rgb(242, 236, 255), COLOR_ACCENT);
        weightingCard.addView(createText("Optionale Doppelgewichtung in Block I", 17, true));
        weightingCard.addView(createText(
                "Nach der Oberstufenverordnung können die Kurshalbjahresergebnisse der ersten beiden Prüfungsfächer in Block I doppelt gewertet werden.",
                13,
                false
        ));

        CheckBox doubleWeight = new CheckBox(this);
        doubleWeight.setText("P1 und P2 in Block I doppelt gewichten");
        doubleWeight.setTextColor(COLOR_TEXT);
        doubleWeight.setChecked(doubleWeightP1P2);
        doubleWeight.setOnCheckedChangeListener((buttonView, isChecked) -> doubleWeightP1P2 = isChecked);
        weightingCard.addView(doubleWeight);
        page.addView(weightingCard);

        Button recalculateButton = createGreenButton("Ergebnis aktualisieren");
        recalculateButton.setOnClickListener(v -> showUpperSecondaryPage());
        page.addView(recalculateButton);

        showInContent(wrapInScrollView(page));
    }

    private UpperSummary calculateUpperSummary() {
        UpperSummary result = new UpperSummary();

        double pointSum = 0;
        int weightedA = 0;
        int rawIncluded = 0;
        int lowWeighted = 0;
        boolean hasZero = false;
        int filledHalfyears = 0;

        for (String subject : upperSubjects) {
            int[] points = getHalfyearPoints(subject);
            boolean[] included = getIncludedFlags(subject);

            boolean doubleSubject = doubleWeightP1P2 &&
                    (matchesExamSubject(subject, 0) || matchesExamSubject(subject, 1));

            int weight = doubleSubject ? 2 : 1;

            for (int h = 0; h < 4; h++) {
                if (points[h] >= 0) {
                    filledHalfyears++;
                }

                if (points[h] >= 0 && included[h]) {
                    rawIncluded++;
                    pointSum += points[h] * weight;
                    weightedA += weight;

                    if (points[h] < 5) {
                        lowWeighted += weight;
                    }
                    if (points[h] == 0) {
                        hasZero = true;
                    }
                }
            }
        }

        result.filledHalfyears = filledHalfyears;
        result.rawIncluded = rawIncluded;
        result.weightedA = weightedA;
        result.lowWeighted = lowWeighted;
        result.hasZero = hasZero;

        if (weightedA > 0) {
            result.blockI = (int) Math.floor(((pointSum / weightedA) * 40.0) + 0.5);
        }

        int examCount = 0;
        int blockII = 0;
        int examsAtLeastFive = 0;

        for (int i = 0; i < examPoints.length; i++) {
            if (examPoints[i] >= 0) {
                examCount++;
                blockII += examPoints[i] * 4;
                if (examPoints[i] >= 5) {
                    examsAtLeastFive++;
                }
            }
        }

        result.examCount = examCount;
        result.blockII = blockII;
        result.blockIIComplete = examCount == 5;
        result.examsAtLeastFive = examsAtLeastFive;
        result.highLevelAtLeastFive =
                (examPoints[0] >= 5) || (examPoints[1] >= 5);

        result.allWrittenAtLeastOne =
                examPoints[0] > 0 &&
                examPoints[1] > 0 &&
                examPoints[2] > 0 &&
                examPoints[3] > 0;

        if (result.blockI >= 0 && result.blockIIComplete) {
            result.total = result.blockI + result.blockII;
        }

        return result;
    }

    private boolean matchesExamSubject(String subject, int examIndex) {
        return examSubjects[examIndex] != null &&
                !examSubjects[examIndex].isEmpty() &&
                subject.equalsIgnoreCase(examSubjects[examIndex]);
    }

    private String buildUpperWarning(UpperSummary summary) {
        ArrayList<String> warnings = new ArrayList<>();

        if (summary.rawIncluded > 0 && summary.rawIncluded < 36) {
            warnings.add("Für Block I sind mindestens 36 Kurshalbjahresergebnisse einzubringen.");
        }
        if (summary.rawIncluded > 40) {
            warnings.add("Es sind mehr als 40 Kurshalbjahresergebnisse ausgewählt.");
        }
        if (summary.hasZero) {
            warnings.add("0 Punkte dürfen nicht in Block I eingebracht werden.");
        }
        if (summary.weightedA > 0 && summary.lowWeighted > Math.floor(summary.weightedA * 0.20)) {
            warnings.add("Mehr als 20 % der berücksichtigten Ergebnisse liegen unter 5 Punkten.");
        }
        if (summary.rawIncluded >= 36 && summary.blockI >= 0 && summary.blockI < 200) {
            warnings.add("Block I liegt unter den erforderlichen 200 Punkten.");
        }

        if (summary.blockIIComplete) {
            if (summary.blockII < 100) {
                warnings.add("Block II liegt unter den erforderlichen 100 Punkten.");
            }
            if (summary.examsAtLeastFive < 3 || !summary.highLevelAtLeastFive) {
                warnings.add("Mindestens drei Prüfungselemente müssen 5 Punkte oder mehr erreichen; darunter mindestens P1 oder P2.");
            }
            if (!summary.allWrittenAtLeastOne) {
                warnings.add("Bei den vier schriftlichen Prüfungselementen muss jeweils mindestens 1 Punkt erreicht werden.");
            }
        }

        return String.join("\n", warnings);
    }

    private String buildHalfYearPreview(int halfyearIndex) {
        ArrayList<String> parts = new ArrayList<>();

        for (String subject : upperSubjects) {
            int value = getHalfyearPoints(subject)[halfyearIndex];
            if (value >= 0) {
                parts.add(subject + " " + value + "P");
                if (parts.size() == 4) {
                    break;
                }
            }
        }

        if (parts.isEmpty()) {
            return "Noch keine Kurshalbjahresergebnisse eingetragen.";
        }

        int totalFilled = countFilledHalfyear(halfyearIndex);
        String preview = String.join("  •  ", parts);

        if (totalFilled > parts.size()) {
            preview += "  •  +" + (totalFilled - parts.size()) + " weitere";
        }

        return preview;
    }

    private int countFilledHalfyear(int halfyearIndex) {
        int count = 0;
        for (String subject : upperSubjects) {
            if (getHalfyearPoints(subject)[halfyearIndex] >= 0) {
                count++;
            }
        }
        return count;
    }

    private String calculateHalfyearAverage(int halfyearIndex) {
        double sum = 0;
        int count = 0;

        for (String subject : upperSubjects) {
            int value = getHalfyearPoints(subject)[halfyearIndex];
            if (value >= 0) {
                sum += value;
                count++;
            }
        }

        if (count == 0) {
            return "–";
        }

        return String.format(Locale.GERMANY, "%.2f", sum / count);
    }

    private String buildExamPreview() {
        ArrayList<String> parts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            if (examPoints[i] >= 0) {
                String subject = examSubjects[i].isEmpty() ? "P" + (i + 1) : examSubjects[i];
                parts.add(subject + " " + examPoints[i] + "P");
            }
        }

        return String.join("  •  ", parts);
    }

    private String getAbiGrade(int totalPoints) {
        if (totalPoints < 300) {
            return "nicht bestanden";
        }
        if (totalPoints >= 823) {
            return "1,0";
        }

        double grade = (17.0 / 3.0) - (totalPoints / 180.0);
        grade = Math.floor((grade + 0.000001) * 10.0) / 10.0;
        grade = Math.max(1.0, Math.min(4.0, grade));

        return String.format(Locale.GERMANY, "%.1f", grade);
    }

    private void syncUpperSubjectsFromClasses() {
        for (int classLevel = 11; classLevel <= 12; classLevel++) {
            for (String subject : getSubjectsForClass(classLevel)) {
                addUpperSubject(subject);
            }
        }
    }

    private void addUpperSubject(String subject) {
        String cleaned = subject.trim();
        if (cleaned.isEmpty()) {
            return;
        }

        for (String existing : upperSubjects) {
            if (existing.equalsIgnoreCase(cleaned)) {
                return;
            }
        }

        upperSubjects.add(cleaned);
        getHalfyearPoints(cleaned);
        getIncludedFlags(cleaned);
    }

    private void removeUpperSubjectIfUnused(String subject) {
        boolean stillUsed = false;

        for (int classLevel = 11; classLevel <= 12; classLevel++) {
            for (String existing : getSubjectsForClass(classLevel)) {
                if (existing.equalsIgnoreCase(subject)) {
                    stillUsed = true;
                    break;
                }
            }
        }

        if (!stillUsed) {
            removeUpperSubject(subject);
        }
    }

    private void removeUpperSubject(String subject) {
        String found = null;
        for (String existing : upperSubjects) {
            if (existing.equalsIgnoreCase(subject)) {
                found = existing;
                break;
            }
        }

        if (found != null) {
            upperSubjects.remove(found);
            String key = normalizeSubjectKey(found);
            upperHalfyearPoints.remove(key);
            upperIncluded.remove(key);

            for (int i = 0; i < examSubjects.length; i++) {
                if (examSubjects[i].equalsIgnoreCase(found)) {
                    examSubjects[i] = "";
                }
            }
        }
    }

    private int[] getHalfyearPoints(String subject) {
        String key = normalizeSubjectKey(subject);

        if (!upperHalfyearPoints.containsKey(key)) {
            upperHalfyearPoints.put(key, new int[]{-1, -1, -1, -1});
        }

        return upperHalfyearPoints.get(key);
    }

    private boolean[] getIncludedFlags(String subject) {
        String key = normalizeSubjectKey(subject);

        if (!upperIncluded.containsKey(key)) {
            upperIncluded.put(key, new boolean[]{true, true, true, true});
        }

        return upperIncluded.get(key);
    }

    private String normalizeSubjectKey(String subject) {
        return subject.trim().toLowerCase(Locale.ROOT);
    }

    private static class UpperSummary {
        int blockI = -1;
        int blockII = 0;
        int total = -1;
        int filledHalfyears = 0;
        int rawIncluded = 0;
        int weightedA = 0;
        int lowWeighted = 0;
        boolean hasZero = false;

        int examCount = 0;
        boolean blockIIComplete = false;
        int examsAtLeastFive = 0;
        boolean highLevelAtLeastFive = false;
        boolean allWrittenAtLeastOne = false;

        boolean isCompleteForGrade() {
            return rawIncluded >= 36 &&
                    rawIncluded <= 40 &&
                    blockI >= 0 &&
                    blockIIComplete &&
                    total >= 0;
        }
    }

    // --------------------------------------------------
    // NOTEN-HELFER
    // --------------------------------------------------

    private ArrayList<Integer> getPossibleGradeValues() {
        ArrayList<Integer> values = new ArrayList<>();
        if (selectedClass <= 10) {
            for (int grade = 1; grade <= 6; grade++) {
                values.add(grade);
            }
        } else {
            for (int points = 15; points >= 0; points--) {
                values.add(points);
            }
        }
        return values;
    }

    private String formatGradeValue(int value) {
        if (selectedClass <= 10) {
            return "Note " + value;
        }
        return value + (value == 1 ? " Punkt" : " Punkte");
    }

    private String calculateAverage(ArrayList<Integer> entries) {
        if (entries.isEmpty()) {
            return "–";
        }

        double sum = 0;
        for (Integer entry : entries) {
            sum += entry;
        }
        double average = sum / entries.size();

        if (selectedClass <= 10) {
            return String.format(Locale.GERMANY, "Ø %.2f", average);
        }
        return String.format(Locale.GERMANY, "Ø %.2f P", average);
    }

    private ArrayList<Integer> getGradeEntries(int classLevel, String subject, boolean written) {
        String key = createGradeKey(classLevel, subject);
        Map<String, ArrayList<Integer>> source = written ? writtenGradesBySubject : oralGradesBySubject;

        if (!source.containsKey(key)) {
            source.put(key, new ArrayList<>());
        }

        return source.get(key);
    }

    private String createGradeKey(int classLevel, String subject) {
        return classLevel + "::" + subject.trim().toLowerCase(Locale.ROOT);
    }

    private int gradeAccent(int value) {
        if (selectedClass <= 10) {
            if (value <= 2) return COLOR_GREEN;
            if (value <= 4) return Color.rgb(203, 132, 25);
            return COLOR_DANGER;
        }

        if (value >= 10) return COLOR_GREEN;
        if (value >= 5) return Color.rgb(203, 132, 25);
        return COLOR_DANGER;
    }

    // --------------------------------------------------
    // FINANZEN / DATENSCHUTZ
    // --------------------------------------------------

    private void showFinancePage() {
        LinearLayout page = createVerticalPage();

        page.addView(createTitle("Finanzen"));
        page.addView(createSubtitle("Dieser Bereich wird als Nächstes aufgebaut."));

        LinearLayout card = createTintedCard(Color.rgb(236, 247, 242), COLOR_GREEN);
        card.addView(createText("Geplant", 20, true));
        card.addView(createText(
                "• Einnahmen und Ausgaben\n• Tages-, Wochen- und Monatsansicht\n• Kategorien\n• Diagramme und Budgets",
                15,
                false
        ));
        page.addView(card);

        showInContent(wrapInScrollView(page));
    }

    private void showPrivacyPage() {
        LinearLayout page = createVerticalPage();

        Button backButton = createBackButton("←  Zurück");
        backButton.setOnClickListener(v -> showHomePage());
        page.addView(backButton);

        page.addView(createTitle("Datenschutz & Sicherheit"));
        page.addView(createSubtitle("Deine Daten bleiben aktuell ausschließlich in der laufenden App-Sitzung."));

        LinearLayout privacyCard = createTintedCard(Color.rgb(236, 247, 242), COLOR_GREEN);
        privacyCard.addView(createText("Aktueller Datenschutzstatus", 19, true));
        privacyCard.addView(createText(
                "✓ Keine Standort-, Kamera-, Mikrofon-, Kontakt- oder Speicherberechtigungen\n" +
                        "✓ Keine Werbung und kein Tracking\n" +
                        "✓ Keine Internetberechtigung\n" +
                        "✓ Unverschlüsselte HTTP-Verbindungen gesperrt\n" +
                        "✓ Android-Cloud-Backups deaktiviert\n" +
                        "✓ Kein Benutzerkonto",
                14,
                false
        ));
        page.addView(privacyCard);

        LinearLayout controlCard = createCard();
        controlCard.addView(createText("Deine Kontrolle", 19, true));
        controlCard.addView(createText(
                "Du kannst alle aktuell angelegten Fächer, Noten, Kurshalbjahresergebnisse und Abiturprüfungen löschen.",
                14,
                false
        ));

        Button clearButton = createDangerButton("Alle lokalen Sitzungsdaten löschen");
        clearButton.setFilterTouchesWhenObscured(true);
        clearButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Daten löschen?")
                .setMessage("Alle aktuell angelegten Fächer, Noten und Oberstufendaten werden aus dieser Sitzung entfernt.")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (dialog, which) -> {
                    subjectsByClass.clear();
                    writtenGradesBySubject.clear();
                    oralGradesBySubject.clear();

                    upperSubjects.clear();
                    upperHalfyearPoints.clear();
                    upperIncluded.clear();

                    Arrays.fill(examSubjects, "");
                    Arrays.fill(examPoints, -1);
                    doubleWeightP1P2 = false;

                    Toast.makeText(this, "Lokale Sitzungsdaten gelöscht.", Toast.LENGTH_SHORT).show();
                    showPrivacyPage();
                })
                .show());

        controlCard.addView(clearButton);
        page.addView(controlCard);

        showInContent(wrapInScrollView(page));
    }

    // --------------------------------------------------
    // DATEN
    // --------------------------------------------------

    private ArrayList<String> getSubjectsForClass(int classLevel) {
        if (!subjectsByClass.containsKey(classLevel)) {
            subjectsByClass.put(classLevel, new ArrayList<>());
        }
        return subjectsByClass.get(classLevel);
    }

    // --------------------------------------------------
    // UI-HELFER
    // --------------------------------------------------

    private LinearLayout createVerticalPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(22), dp(20), dp(28));
        page.setBackgroundColor(COLOR_BACKGROUND);
        return page;
    }

    private TextView createTitle(String text) {
        TextView title = createText(text, 30, true);
        title.setTextColor(COLOR_TEXT);
        title.setPadding(0, dp(2), 0, dp(6));
        return title;
    }

    private TextView createSectionTitle(String text) {
        TextView title = createText(text, 20, true);
        title.setTextColor(COLOR_TEXT);
        title.setPadding(0, dp(16), 0, dp(6));
        return title;
    }

    private TextView createSubtitle(String text) {
        TextView subtitle = createText(text, 15, false);
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, 0, 0, dp(12));
        return subtitle;
    }

    private TextView createText(String text, float size, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(COLOR_TEXT);
        textView.setLineSpacing(0, 1.12f);

        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        textView.setPadding(0, dp(4), 0, dp(4));
        return textView;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(createRoundedBackground(Color.WHITE, dp(20), dp(1), COLOR_BORDER));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(7), 0, dp(7));
        card.setLayoutParams(params);

        return card;
    }

    private LinearLayout createTintedCard(int backgroundColor, int strokeColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(createRoundedBackground(backgroundColor, dp(22), dp(1), lighten(strokeColor)));
        card.setElevation(dp(1));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(7), 0, dp(7));
        card.setLayoutParams(params);

        return card;
    }

    private TextView createChip(String text, int accent) {
        TextView chip = createText(text, 13, true);
        chip.setTextColor(accent);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(11), dp(6), dp(11), dp(6));
        chip.setBackground(createRoundedBackground(lightBackground(accent), dp(50), 0, Color.TRANSPARENT));
        return chip;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(createRoundedBackground(COLOR_PRIMARY, dp(15), 0, Color.TRANSPARENT));
        return button;
    }

    private Button createAccentButton(String text) {
        Button button = createPrimaryButton(text);
        button.setBackground(createRoundedBackground(COLOR_ACCENT, dp(15), 0, Color.TRANSPARENT));
        return button;
    }

    private Button createGreenButton(String text) {
        Button button = createPrimaryButton(text);
        button.setBackground(createRoundedBackground(COLOR_GREEN, dp(15), 0, Color.TRANSPARENT));
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(COLOR_PRIMARY_DARK);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(createRoundedBackground(Color.rgb(238, 242, 252), dp(14), dp(1), Color.rgb(205, 214, 237)));
        return button;
    }

    private Button createDangerButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(COLOR_DANGER);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(createRoundedBackground(Color.rgb(255, 239, 239), dp(14), dp(1), Color.rgb(238, 190, 190)));
        return button;
    }

    private Button createSmallDangerButton(String text) {
        Button button = createDangerButton(text);
        button.setTextSize(20);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private Button createBackButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(COLOR_PRIMARY_DARK);
        button.setTextSize(13);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(42)
        );
        params.setMargins(0, 0, 0, dp(6));
        button.setLayoutParams(params);

        return button;
    }

    private GradientDrawable createRoundedBackground(int fill, float radius, int strokeWidth, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            drawable.setStroke(strokeWidth, strokeColor);
        }
        return drawable;
    }

    private int lightBackground(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = (int) (r + (255 - r) * 0.88);
        g = (int) (g + (255 - g) * 0.88);
        b = (int) (b + (255 - b) * 0.88);

        return Color.rgb(r, g, b);
    }

    private int lighten(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = (int) (r + (255 - r) * 0.66);
        g = (int) (g + (255 - g) * 0.66);
        b = (int) (b + (255 - b) * 0.66);

        return Color.rgb(r, g, b);
    }

    private ScrollView wrapInScrollView(View child) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(child);
        return scrollView;
    }

    private void showInContent(View view) {
        contentContainer.removeAllViews();
        contentContainer.addView(view, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
