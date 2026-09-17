package de.notenapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(244, 247, 251);
    private static final int CARD = Color.WHITE;
    private static final int PRIMARY = Color.rgb(66, 82, 180);
    private static final int PRIMARY_DARK = Color.rgb(42, 55, 145);
    private static final int PRIMARY_SOFT = Color.rgb(232, 235, 252);
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final int MUTED = Color.rgb(107, 114, 128);
    private static final int BORDER = Color.rgb(226, 232, 240);
    private static final int SUCCESS = Color.rgb(22, 163, 74);
    private static final int SUCCESS_SOFT = Color.rgb(236, 253, 245);
    private static final int DANGER = Color.rgb(220, 38, 38);
    private static final int DANGER_SOFT = Color.rgb(254, 242, 242);
    private static final int WARNING_SOFT = Color.rgb(255, 247, 237);

    private FrameLayout contentContainer;
    private Button homeNavButton;
    private Button gradesNavButton;
    private Button financeNavButton;

    private int selectedClass = 12;

    private final Map<Integer, ArrayList<String>> subjectsByClass = new HashMap<>();
    private final Map<String, ArrayList<Integer>> writtenGradesBySubject = new HashMap<>();
    private final Map<String, ArrayList<Integer>> oralGradesBySubject = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

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
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(12), dp(8), dp(12), dp(10));
        wrapper.setBackgroundColor(CARD);
        wrapper.setElevation(dp(12));

        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        wrapper.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        ));

        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER);
        navigation.setPadding(0, dp(8), 0, 0);

        gradesNavButton = createNavButton("Noten");
        homeNavButton = createNavButton("Home");
        financeNavButton = createNavButton("Finanzen");

        gradesNavButton.setOnClickListener(v -> showGradesPage());
        homeNavButton.setOnClickListener(v -> showHomePage());
        financeNavButton.setOnClickListener(v -> showFinancePage());

        navigation.addView(gradesNavButton, navParams());
        navigation.addView(homeNavButton, navParams());
        navigation.addView(financeNavButton, navParams());
        wrapper.addView(navigation);

        return wrapper;
    }

    private LinearLayout.LayoutParams navParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(46), 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private Button createNavButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setPadding(dp(8), 0, dp(8), 0);
        styleNavButton(button, false);
        return button;
    }

    private void setActiveNavigation(String page) {
        styleNavButton(homeNavButton, "home".equals(page));
        styleNavButton(gradesNavButton, "grades".equals(page));
        styleNavButton(financeNavButton, "finance".equals(page));
    }

    private void styleNavButton(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.setTextColor(active ? Color.WHITE : MUTED);
        button.setBackground(roundedBackground(active ? PRIMARY : Color.TRANSPARENT, 14, 0, Color.TRANSPARENT));
        button.setElevation(active ? dp(2) : 0);
    }

    // --------------------------------------------------
    // HOME
    // --------------------------------------------------

    private void showHomePage() {
        setActiveNavigation("home");

        LinearLayout page = createVerticalPage();
        page.addView(createEyebrow("NOTENAPP"));
        page.addView(createTitle("Dein Überblick"));
        page.addView(createSubtitle("Alles Wichtige für deine Schule an einem Ort."));

        LinearLayout heroCard = new LinearLayout(this);
        heroCard.setOrientation(LinearLayout.VERTICAL);
        heroCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        heroCard.setBackground(gradientBackground(
                new int[]{PRIMARY_DARK, PRIMARY},
                22
        ));
        heroCard.setElevation(dp(5));
        LinearLayout.LayoutParams heroParams = fullWidthWrap();
        heroParams.setMargins(0, dp(8), 0, dp(14));
        heroCard.setLayoutParams(heroParams);

        TextView heroLabel = createText("Aktuelle Klassenstufe", 13, true);
        heroLabel.setTextColor(Color.rgb(219, 224, 255));
        heroCard.addView(heroLabel);

        TextView heroClass = createText("Klasse " + selectedClass, 34, true);
        heroClass.setTextColor(Color.WHITE);
        heroClass.setPadding(0, dp(2), 0, dp(14));
        heroCard.addView(heroClass);

        LinearLayout heroStats = new LinearLayout(this);
        heroStats.setOrientation(LinearLayout.HORIZONTAL);
        heroStats.addView(createHeroStat("Fächer", String.valueOf(getSubjectsForClass(selectedClass).size())),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        heroStats.addView(createHeroStat("Einträge", String.valueOf(countGradeEntriesForClass(selectedClass))),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        heroCard.addView(heroStats);

        Button heroButton = createPrimaryButton("Noten verwalten");
        GradientDrawable heroButtonBg = roundedBackground(Color.WHITE, 14, 0, Color.TRANSPARENT);
        heroButton.setBackground(heroButtonBg);
        heroButton.setTextColor(PRIMARY_DARK);
        heroButton.setOnClickListener(v -> showGradesPage());
        LinearLayout.LayoutParams heroButtonParams = fullWidthHeight(48);
        heroButtonParams.setMargins(0, dp(16), 0, 0);
        heroCard.addView(heroButton, heroButtonParams);

        page.addView(heroCard);

        page.addView(createSectionHeading("Schnellzugriff", "Was möchtest du machen?"));

        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout gradesCard = createMiniCard("Noten", "Fächer & Bewertungen", PRIMARY_SOFT, PRIMARY);
        gradesCard.setOnClickListener(v -> showGradesPage());
        LinearLayout financeCard = createMiniCard("Finanzen", "Demnächst verfügbar", WARNING_SOFT, Color.rgb(194, 65, 12));
        financeCard.setOnClickListener(v -> showFinancePage());

        LinearLayout.LayoutParams miniLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        miniLeft.setMargins(0, 0, dp(6), 0);
        LinearLayout.LayoutParams miniRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        miniRight.setMargins(dp(6), 0, 0, 0);
        quickRow.addView(gradesCard, miniLeft);
        quickRow.addView(financeCard, miniRight);
        page.addView(quickRow);

        LinearLayout privacyCard = createCard();
        LinearLayout privacyHeader = new LinearLayout(this);
        privacyHeader.setOrientation(LinearLayout.HORIZONTAL);
        privacyHeader.setGravity(Gravity.CENTER_VERTICAL);

        TextView privacyIcon = createCircleBadge("✓", SUCCESS_SOFT, SUCCESS);
        privacyHeader.addView(privacyIcon);

        LinearLayout privacyText = new LinearLayout(this);
        privacyText.setOrientation(LinearLayout.VERTICAL);
        privacyText.setPadding(dp(12), 0, 0, 0);
        privacyText.addView(createText("Datenschutz aktiv", 17, true));
        TextView privacyInfo = createText("Kein Tracking • keine Werbung • keine Internetberechtigung", 13, false);
        privacyInfo.setTextColor(MUTED);
        privacyText.addView(privacyInfo);
        privacyHeader.addView(privacyText, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        privacyCard.addView(privacyHeader);
        Button privacyButton = createSecondaryButton("Sicherheit ansehen");
        privacyButton.setOnClickListener(v -> showPrivacyPage());
        LinearLayout.LayoutParams privacyButtonParams = fullWidthHeight(46);
        privacyButtonParams.setMargins(0, dp(12), 0, 0);
        privacyCard.addView(privacyButton, privacyButtonParams);
        page.addView(privacyCard);

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout createHeroStat(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(10), dp(14), dp(10));
        box.setBackground(roundedBackground(Color.argb(38, 255, 255, 255), 14, 0, Color.TRANSPARENT));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(3), 0, dp(3), 0);
        box.setLayoutParams(params);

        TextView valueText = createText(value, 21, true);
        valueText.setTextColor(Color.WHITE);
        box.addView(valueText);
        TextView labelText = createText(label, 12, false);
        labelText.setTextColor(Color.rgb(224, 228, 255));
        box.addView(labelText);
        return box;
    }

    private LinearLayout createMiniCard(String title, String subtitle, int bgColor, int accentColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundedBackground(bgColor, 18, 0, Color.TRANSPARENT));
        card.setClickable(true);
        card.setFocusable(true);

        TextView dot = createCircleBadge("•", Color.WHITE, accentColor);
        card.addView(dot);
        TextView titleText = createText(title, 18, true);
        titleText.setPadding(0, dp(10), 0, 0);
        card.addView(titleText);
        TextView subtitleText = createText(subtitle, 13, false);
        subtitleText.setTextColor(MUTED);
        card.addView(subtitleText);
        return card;
    }

    // --------------------------------------------------
    // NOTENÜBERSICHT
    // --------------------------------------------------

    private void showGradesPage() {
        setActiveNavigation("grades");

        LinearLayout page = createVerticalPage();
        page.addView(createEyebrow("SCHULE"));
        page.addView(createTitle("Noten"));
        page.addView(createSubtitle("Wähle deine Klasse und verwalte deine Fächer."));

        LinearLayout classCard = createCard();
        classCard.addView(createLabel("Klassenstufe"));

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
        classSpinner.setBackground(roundedBackground(Color.rgb(248, 250, 252), 12, 1, BORDER));
        classSpinner.setPadding(dp(12), dp(4), dp(12), dp(4));
        classCard.addView(classSpinner, fullWidthHeight(52));

        TextView gradingPill = createPill(
                selectedClass <= 10 ? "Notensystem 1–6" : "Punktesystem 0–15",
                PRIMARY_SOFT,
                PRIMARY
        );
        LinearLayout.LayoutParams gradingParams = wrapParams();
        gradingParams.setMargins(0, dp(10), 0, 0);
        classCard.addView(gradingPill, gradingParams);
        page.addView(classCard);

        classSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int newClass = classes.get(position);
                if (newClass != selectedClass) {
                    selectedClass = newClass;
                    showGradesPage();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        page.addView(createSectionHeading("Deine Fächer", getSubjectsForClass(selectedClass).size() + " angelegt"));

        ArrayList<String> subjects = getSubjectsForClass(selectedClass);
        if (subjects.isEmpty()) {
            LinearLayout emptyCard = createCard();
            TextView emptyIcon = createCircleBadge("+", PRIMARY_SOFT, PRIMARY);
            emptyCard.addView(emptyIcon);
            TextView emptyTitle = createText("Noch keine Fächer", 18, true);
            emptyTitle.setPadding(0, dp(10), 0, 0);
            emptyCard.addView(emptyTitle);
            TextView emptyInfo = createText("Lege dein erstes Fach an, zum Beispiel Mathematik oder Deutsch.", 14, false);
            emptyInfo.setTextColor(MUTED);
            emptyCard.addView(emptyInfo);
            page.addView(emptyCard);
        } else {
            for (String subject : new ArrayList<>(subjects)) {
                page.addView(createSubjectCard(subject));
            }
        }

        LinearLayout addCard = createCard();
        addCard.addView(createText("Neues Fach", 17, true));

        EditText subjectInput = new EditText(this);
        subjectInput.setHint("z. B. Mathematik");
        subjectInput.setSingleLine(true);
        subjectInput.setTextColor(TEXT);
        subjectInput.setHintTextColor(MUTED);
        subjectInput.setBackground(roundedBackground(Color.rgb(248, 250, 252), 12, 1, BORDER));
        subjectInput.setPadding(dp(14), 0, dp(14), 0);
        subjectInput.setFilterTouchesWhenObscured(true);
        LinearLayout.LayoutParams inputParams = fullWidthHeight(52);
        inputParams.setMargins(0, dp(10), 0, dp(10));
        addCard.addView(subjectInput, inputParams);

        Button addButton = createPrimaryButton("Fach hinzufügen");
        addButton.setFilterTouchesWhenObscured(true);
        addButton.setOnClickListener(v -> {
            String newSubject = subjectInput.getText().toString().trim();
            if (newSubject.isEmpty()) {
                Toast.makeText(this, "Bitte gib einen Fachnamen ein.", Toast.LENGTH_SHORT).show();
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
            subjectInput.setText("");
            showGradesPage();
        });
        addCard.addView(addButton, fullWidthHeight(48));
        page.addView(addCard);

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout createSubjectCard(String subject) {
        LinearLayout card = createCard();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView initial = createCircleBadge(
                subject.substring(0, 1).toUpperCase(Locale.GERMANY),
                PRIMARY_SOFT,
                PRIMARY
        );
        top.addView(initial);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setPadding(dp(12), 0, dp(8), 0);
        textBox.addView(createText(subject, 18, true));

        int writtenCount = getGradeEntries(selectedClass, subject, true).size();
        int oralCount = getGradeEntries(selectedClass, subject, false).size();
        TextView meta = createText((writtenCount + oralCount) + " Bewertungen", 13, false);
        meta.setTextColor(MUTED);
        textBox.addView(meta);
        top.addView(textBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = createText("›", 30, false);
        arrow.setTextColor(PRIMARY);
        arrow.setGravity(Gravity.CENTER);
        top.addView(arrow, new LinearLayout.LayoutParams(dp(30), dp(46)));

        card.addView(top);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> showSubjectPage(subject));

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.END);
        actionRow.setPadding(0, dp(10), 0, 0);

        Button openButton = createSecondaryButton("Öffnen");
        openButton.setOnClickListener(v -> showSubjectPage(subject));
        LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        openParams.setMargins(0, 0, dp(5), 0);
        actionRow.addView(openButton, openParams);

        Button deleteButton = createDangerButton("Löschen");
        deleteButton.setFilterTouchesWhenObscured(true);
        deleteButton.setOnClickListener(v -> confirmDeleteSubject(subject));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        deleteParams.setMargins(dp(5), 0, 0, 0);
        actionRow.addView(deleteButton, deleteParams);

        card.addView(actionRow);
        return card;
    }

    private void confirmDeleteSubject(String subject) {
        new AlertDialog.Builder(this)
                .setTitle(subject + " löschen?")
                .setMessage("Das Fach und alle dazugehörigen Noteneinträge werden aus dieser Sitzung entfernt.")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (dialog, which) -> {
                    getSubjectsForClass(selectedClass).remove(subject);
                    writtenGradesBySubject.remove(createGradeKey(selectedClass, subject));
                    oralGradesBySubject.remove(createGradeKey(selectedClass, subject));
                    showGradesPage();
                })
                .show();
    }

    // --------------------------------------------------
    // FACHSEITE
    // --------------------------------------------------

    private void showSubjectPage(String subject) {
        setActiveNavigation("grades");

        LinearLayout page = createVerticalPage();
        page.addView(createBackButton("Zurück zu den Fächern", this::showGradesPage));
        page.addView(createEyebrow("KLASSE " + selectedClass));
        page.addView(createTitle(subject));
        page.addView(createSubtitle(
                selectedClass <= 10 ? "Bewertung mit Schulnoten von 1 bis 6" : "Bewertung mit 0 bis 15 Notenpunkten"
        ));

        int totalEntries = getGradeEntries(selectedClass, subject, true).size()
                + getGradeEntries(selectedClass, subject, false).size();
        LinearLayout summary = createCard();
        LinearLayout summaryRow = new LinearLayout(this);
        summaryRow.setOrientation(LinearLayout.HORIZONTAL);
        summaryRow.setGravity(Gravity.CENTER_VERTICAL);
        summaryRow.addView(createCircleBadge(String.valueOf(totalEntries), PRIMARY_SOFT, PRIMARY));
        LinearLayout summaryText = new LinearLayout(this);
        summaryText.setOrientation(LinearLayout.VERTICAL);
        summaryText.setPadding(dp(12), 0, 0, 0);
        summaryText.addView(createText("Gesamte Einträge", 16, true));
        TextView summarySub = createText("Schriftlich und mündlich zusammen", 13, false);
        summarySub.setTextColor(MUTED);
        summaryText.addView(summarySub);
        summaryRow.addView(summaryText);
        summary.addView(summaryRow);
        page.addView(summary);

        String writtenTitle = selectedClass >= 11 ? "Klausuren" : "Klassenarbeiten";
        page.addView(createGradeCard(subject, writtenTitle, true));
        page.addView(createGradeCard(subject, "Mündliche Noten", false));

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout createGradeCard(String subject, String title, boolean written) {
        LinearLayout card = createCard();

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = createCircleBadge(written ? "S" : "M", written ? PRIMARY_SOFT : SUCCESS_SOFT, written ? PRIMARY : SUCCESS);
        heading.addView(icon);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(dp(12), 0, 0, 0);
        titleBox.addView(createText(title, 18, true));
        TextView typeInfo = createText(written ? "Schriftliche Bewertungen" : "Mündliche Bewertungen", 13, false);
        typeInfo.setTextColor(MUTED);
        titleBox.addView(typeInfo);
        heading.addView(titleBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(heading);

        ArrayList<Integer> entries = getGradeEntries(selectedClass, subject, written);

        if (entries.isEmpty()) {
            TextView empty = createText("Noch keine Einträge vorhanden.", 14, false);
            empty.setTextColor(MUTED);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(18), 0, dp(18));
            card.addView(empty);
        } else {
            TextView average = createPill(
                    "Ø  " + calculateAverage(entries),
                    written ? PRIMARY_SOFT : SUCCESS_SOFT,
                    written ? PRIMARY : SUCCESS
            );
            LinearLayout.LayoutParams averageParams = wrapParams();
            averageParams.setMargins(0, dp(14), 0, dp(8));
            card.addView(average, averageParams);

            for (int i = 0; i < entries.size(); i++) {
                final int index = i;
                int value = entries.get(i);
                card.addView(createGradeRow(subject, entries, index, value));
            }
        }

        View spacer = new View(this);
        card.addView(spacer, new LinearLayout.LayoutParams(1, dp(8)));

        LinearLayout addArea = new LinearLayout(this);
        addArea.setOrientation(LinearLayout.VERTICAL);
        addArea.setPadding(dp(12), dp(12), dp(12), dp(12));
        addArea.setBackground(roundedBackground(Color.rgb(248, 250, 252), 14, 1, BORDER));

        TextView chooseLabel = createText(selectedClass <= 10 ? "Neue Note" : "Neue Notenpunkte", 14, true);
        addArea.addView(chooseLabel);

        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        addRow.setGravity(Gravity.CENTER_VERTICAL);
        addRow.setPadding(0, dp(8), 0, 0);

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
        gradeSpinner.setBackground(roundedBackground(Color.WHITE, 12, 1, BORDER));
        gradeSpinner.setPadding(dp(10), 0, dp(10), 0);

        Button addGradeButton = createPrimaryButton("Hinzufügen");
        addGradeButton.setFilterTouchesWhenObscured(true);
        addGradeButton.setOnClickListener(v -> {
            int position = gradeSpinner.getSelectedItemPosition();
            if (position >= 0 && position < possibleValues.size()) {
                entries.add(possibleValues.get(position));
                showSubjectPage(subject);
            }
        });

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        spinnerParams.setMargins(0, 0, dp(5), 0);
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        addParams.setMargins(dp(5), 0, 0, 0);
        addRow.addView(gradeSpinner, spinnerParams);
        addRow.addView(addGradeButton, addParams);
        addArea.addView(addRow);
        card.addView(addArea);

        return card;
    }

    private LinearLayout createGradeRow(String subject, ArrayList<Integer> entries, int index, int value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(9), dp(8), dp(9));
        row.setBackground(roundedBackground(Color.rgb(250, 251, 253), 12, 1, BORDER));
        LinearLayout.LayoutParams rowParams = fullWidthWrap();
        rowParams.setMargins(0, dp(4), 0, dp(4));
        row.setLayoutParams(rowParams);

        TextView numberBadge = createPill(String.valueOf(index + 1), PRIMARY_SOFT, PRIMARY);
        row.addView(numberBadge);

        TextView valueText = createText(formatGradeValue(value), 16, true);
        valueText.setPadding(dp(12), 0, 0, 0);
        row.addView(valueText, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button removeButton = createIconButton("×");
        removeButton.setContentDescription("Eintrag löschen");
        removeButton.setFilterTouchesWhenObscured(true);
        removeButton.setOnClickListener(v -> {
            entries.remove(index);
            showSubjectPage(subject);
        });
        row.addView(removeButton, new LinearLayout.LayoutParams(dp(40), dp(40)));
        return row;
    }

    // --------------------------------------------------
    // FINANZEN
    // --------------------------------------------------

    private void showFinancePage() {
        setActiveNavigation("finance");

        LinearLayout page = createVerticalPage();
        page.addView(createEyebrow("DEMNÄCHST"));
        page.addView(createTitle("Finanzen"));
        page.addView(createSubtitle("Dein persönlicher Finanzbereich wird hier Schritt für Schritt aufgebaut."));

        LinearLayout hero = createCard();
        TextView icon = createCircleBadge("€", WARNING_SOFT, Color.rgb(194, 65, 12));
        hero.addView(icon);
        TextView heading = createText("Finanzmanager in Vorbereitung", 20, true);
        heading.setPadding(0, dp(12), 0, dp(4));
        hero.addView(heading);
        TextView description = createText(
                "Geplant sind Einnahmen und Ausgaben, Tages-, Wochen- und Monatsübersichten sowie einfache Statistiken.",
                15,
                false
        );
        description.setTextColor(MUTED);
        hero.addView(description);
        page.addView(hero);

        LinearLayout features = createCard();
        features.addView(createText("Geplante Funktionen", 17, true));
        features.addView(createFeatureRow("T", "Tagesübersicht", "Was du heute ausgegeben hast"));
        features.addView(createFeatureRow("W", "Wochenübersicht", "Deine Ausgaben der Woche"));
        features.addView(createFeatureRow("M", "Monatsübersicht", "Entwicklung über den Monat"));
        page.addView(features);

        showInContent(wrapInScrollView(page));
    }

    private LinearLayout createFeatureRow(String badge, String title, String subtitle) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        row.addView(createCircleBadge(badge, PRIMARY_SOFT, PRIMARY));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(12), 0, 0, 0);
        text.addView(createText(title, 15, true));
        TextView subtitleView = createText(subtitle, 13, false);
        subtitleView.setTextColor(MUTED);
        text.addView(subtitleView);
        row.addView(text);
        return row;
    }

    // --------------------------------------------------
    // DATENSCHUTZ
    // --------------------------------------------------

    private void showPrivacyPage() {
        setActiveNavigation("home");

        LinearLayout page = createVerticalPage();
        page.addView(createBackButton("Zurück zur Startseite", this::showHomePage));
        page.addView(createEyebrow("SICHERHEIT"));
        page.addView(createTitle("Datenschutz"));
        page.addView(createSubtitle("Du behältst die Kontrolle über deine Daten."));

        LinearLayout statusCard = createCard();
        LinearLayout statusHeader = new LinearLayout(this);
        statusHeader.setOrientation(LinearLayout.HORIZONTAL);
        statusHeader.setGravity(Gravity.CENTER_VERTICAL);
        statusHeader.addView(createCircleBadge("✓", SUCCESS_SOFT, SUCCESS));
        TextView statusTitle = createText("Sicherer aktueller Stand", 18, true);
        statusTitle.setPadding(dp(12), 0, 0, 0);
        statusHeader.addView(statusTitle);
        statusCard.addView(statusHeader);

        String[] securityPoints = {
                "Keine Standort-, Kamera- oder Mikrofonberechtigungen",
                "Keine Werbung und kein Tracking",
                "Keine Internetberechtigung",
                "HTTP-Verbindungen technisch gesperrt",
                "Cloud-Backups deaktiviert",
                "Aktuell kein Benutzerkonto"
        };
        for (String point : securityPoints) {
            TextView line = createText("•  " + point, 14, false);
            line.setTextColor(MUTED);
            line.setPadding(0, dp(6), 0, dp(2));
            statusCard.addView(line);
        }
        page.addView(statusCard);

        LinearLayout controlCard = createCard();
        controlCard.addView(createText("Deine Kontrolle", 18, true));
        TextView controlInfo = createText(
                "Du kannst alle aktuell angelegten Fächer und Noteneinträge dieser Sitzung vollständig löschen.",
                14,
                false
        );
        controlInfo.setTextColor(MUTED);
        controlInfo.setPadding(0, dp(4), 0, dp(12));
        controlCard.addView(controlInfo);

        Button clearButton = createDangerButton("Alle lokalen Daten löschen");
        clearButton.setFilterTouchesWhenObscured(true);
        clearButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Alle Daten löschen?")
                .setMessage("Alle aktuell angelegten Fächer und Noteneinträge werden aus dieser Sitzung entfernt.")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (dialog, which) -> {
                    subjectsByClass.clear();
                    writtenGradesBySubject.clear();
                    oralGradesBySubject.clear();
                    Toast.makeText(this, "Lokale Sitzungsdaten gelöscht.", Toast.LENGTH_SHORT).show();
                    showPrivacyPage();
                })
                .show());
        controlCard.addView(clearButton, fullWidthHeight(48));
        page.addView(controlCard);

        showInContent(wrapInScrollView(page));
    }

    // --------------------------------------------------
    // DATEN / BERECHNUNGEN
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
            return String.format(Locale.GERMANY, "%.2f", average);
        }
        return String.format(Locale.GERMANY, "%.2f Punkte", average);
    }

    private int countGradeEntriesForClass(int classLevel) {
        int total = 0;
        for (String subject : getSubjectsForClass(classLevel)) {
            total += getGradeEntries(classLevel, subject, true).size();
            total += getGradeEntries(classLevel, subject, false).size();
        }
        return total;
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

    private ArrayList<String> getSubjectsForClass(int classLevel) {
        if (!subjectsByClass.containsKey(classLevel)) {
            subjectsByClass.put(classLevel, new ArrayList<>());
        }
        return subjectsByClass.get(classLevel);
    }

    // --------------------------------------------------
    // UI-HILFSMETHODEN
    // --------------------------------------------------

    private LinearLayout createVerticalPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(22), dp(18), dp(24));
        page.setBackgroundColor(BG);
        return page;
    }

    private TextView createEyebrow(String text) {
        TextView eyebrow = createText(text, 12, true);
        eyebrow.setTextColor(PRIMARY);
        eyebrow.setLetterSpacing(0.09f);
        eyebrow.setPadding(0, 0, 0, dp(2));
        return eyebrow;
    }

    private TextView createTitle(String text) {
        TextView title = createText(text, 31, true);
        title.setTextColor(TEXT);
        title.setPadding(0, 0, 0, dp(4));
        return title;
    }

    private TextView createSubtitle(String text) {
        TextView subtitle = createText(text, 15, false);
        subtitle.setTextColor(MUTED);
        subtitle.setPadding(0, 0, 0, dp(14));
        return subtitle;
    }

    private TextView createLabel(String text) {
        TextView label = createText(text, 14, true);
        label.setTextColor(TEXT);
        label.setPadding(0, 0, 0, dp(8));
        return label;
    }

    private LinearLayout createSectionHeading(String title, String subtitle) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(10), 0, dp(8));
        box.addView(createText(title, 19, true));
        TextView subtitleText = createText(subtitle, 13, false);
        subtitleText.setTextColor(MUTED);
        box.addView(subtitleText);
        return box;
    }

    private TextView createText(String text, float size, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(TEXT);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        textView.setPadding(0, dp(2), 0, dp(2));
        return textView;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundedBackground(CARD, 18, 1, BORDER));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams params = fullWidthWrap();
        params.setMargins(0, dp(6), 0, dp(8));
        card.setLayoutParams(params);
        return card;
    }

    private TextView createCircleBadge(String text, int backgroundColor, int textColor) {
        TextView badge = createText(text, 16, true);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(textColor);
        badge.setBackground(roundedBackground(backgroundColor, 100, 0, Color.TRANSPARENT));
        badge.setMinWidth(dp(42));
        badge.setMinHeight(dp(42));
        return badge;
    }

    private TextView createPill(String text, int backgroundColor, int textColor) {
        TextView pill = createText(text, 13, true);
        pill.setTextColor(textColor);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(12), dp(7), dp(12), dp(7));
        pill.setBackground(roundedBackground(backgroundColor, 100, 0, Color.TRANSPARENT));
        return pill;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(Color.WHITE);
        button.setBackground(roundedBackground(PRIMARY, 14, 0, Color.TRANSPARENT));
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setElevation(dp(2));
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(PRIMARY);
        button.setBackground(roundedBackground(PRIMARY_SOFT, 14, 0, Color.TRANSPARENT));
        button.setPadding(dp(10), 0, dp(10), 0);
        return button;
    }

    private Button createDangerButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(DANGER);
        button.setBackground(roundedBackground(DANGER_SOFT, 14, 0, Color.TRANSPARENT));
        button.setPadding(dp(10), 0, dp(10), 0);
        return button;
    }

    private Button createIconButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(22);
        button.setTextColor(DANGER);
        button.setBackground(roundedBackground(DANGER_SOFT, 100, 0, Color.TRANSPARENT));
        button.setPadding(0, 0, 0, dp(2));
        return button;
    }

    private Button createBackButton(String text, Runnable action) {
        Button button = createSecondaryButton("‹  " + text);
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = wrapHeight(44);
        params.setMargins(0, 0, 0, dp(12));
        button.setLayoutParams(params);
        return button;
    }

    private GradientDrawable roundedBackground(int color, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable gradientBackground(int[] colors, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private ScrollView wrapInScrollView(View child) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BG);
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

    private LinearLayout.LayoutParams fullWidthWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams fullWidthHeight(int heightDp) {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp)
        );
    }

    private LinearLayout.LayoutParams wrapHeight(int heightDp) {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(heightDp)
        );
    }

    private LinearLayout.LayoutParams wrapParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
