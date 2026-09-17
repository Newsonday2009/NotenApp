package de.notenapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private FrameLayout contentContainer;
    private int selectedClass = 12;
    private final Map<Integer, ArrayList<String>> subjectsByClass = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 245, 245));

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

    private View createBottomNavigation() {
        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER);
        navigation.setPadding(dp(8), dp(8), dp(8), dp(8));
        navigation.setBackgroundColor(Color.WHITE);

        Button gradesButton = createNavButton("Noten");
        Button homeButton = createNavButton("Home");
        Button financeButton = createNavButton("Finanzen");

        gradesButton.setOnClickListener(v -> showGradesPage());
        homeButton.setOnClickListener(v -> showHomePage());
        financeButton.setOnClickListener(v -> showFinancePage());

        navigation.addView(gradesButton, new LinearLayout.LayoutParams(0, dp(48), 1f));
        navigation.addView(homeButton, new LinearLayout.LayoutParams(0, dp(48), 1f));
        navigation.addView(financeButton, new LinearLayout.LayoutParams(0, dp(48), 1f));

        return navigation;
    }

    private Button createNavButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private void showHomePage() {
        LinearLayout page = createVerticalPage();

        page.addView(createTitle("Startseite"));
        page.addView(createSubtitle("Deine Schulübersicht"));

        LinearLayout schoolCard = createCard();
        TextView classLabel = createText("Klassenstufe: " + selectedClass, 19, true);
        TextView subjectCount = createText(
                "Angelegte Fächer: " + getSubjectsForClass(selectedClass).size(),
                16,
                false
        );
        Button openGrades = new Button(this);
        openGrades.setText("Zu meinen Fächern");
        openGrades.setAllCaps(false);
        openGrades.setOnClickListener(v -> showGradesPage());

        schoolCard.addView(classLabel);
        schoolCard.addView(subjectCount);
        schoolCard.addView(openGrades);

        LinearLayout financeCard = createCard();
        financeCard.addView(createText("Finanzen", 19, true));
        financeCard.addView(createText("Dieser Bereich wird später ergänzt.", 15, false));

        LinearLayout securityCard = createCard();
        securityCard.addView(createText("Datenschutz & Sicherheit", 19, true));
        securityCard.addView(createText(
                "Keine Werbung, kein Tracking und aktuell keine Übertragung deiner Daten.",
                15,
                false
        ));
        Button privacyButton = new Button(this);
        privacyButton.setText("Datenschutz ansehen");
        privacyButton.setAllCaps(false);
        privacyButton.setOnClickListener(v -> showPrivacyPage());
        securityCard.addView(privacyButton);

        page.addView(schoolCard);
        page.addView(financeCard);
        page.addView(securityCard);

        showInContent(wrapInScrollView(page));
    }

    private void showGradesPage() {
        LinearLayout page = createVerticalPage();
        page.addView(createTitle("Noten"));
        page.addView(createText("Klassenstufe", 16, true));

        Spinner classSpinner = new Spinner(this);
        ArrayList<Integer> classes = new ArrayList<>();
        for (int i = 5; i <= 13; i++) {
            classes.add(i);
        }
        ArrayAdapter<Integer> classAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                classes
        );
        classSpinner.setAdapter(classAdapter);
        classSpinner.setSelection(selectedClass - 5);
        page.addView(classSpinner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView subjectsTitle = createText("Fächer", 20, true);
        subjectsTitle.setPadding(0, dp(14), 0, dp(6));
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
        page.addView(subjectList, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        EditText subjectInput = new EditText(this);
        subjectInput.setHint("Neues Fach, z. B. Mathematik");
        // Verhindert, dass überlagerte/tapjacking-artige Touches auf dieses Eingabefeld wirken.
        subjectInput.setFilterTouchesWhenObscured(true);
        page.addView(subjectInput);

        Button addButton = new Button(this);
        addButton.setText("Fach hinzufügen");
        addButton.setAllCaps(false);
        addButton.setFilterTouchesWhenObscured(true);
        page.addView(addButton);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button openButton = new Button(this);
        openButton.setText("Fach öffnen");
        openButton.setAllCaps(false);
        openButton.setEnabled(false);

        Button deleteButton = new Button(this);
        deleteButton.setText("Löschen");
        deleteButton.setAllCaps(false);
        deleteButton.setEnabled(false);
        deleteButton.setFilterTouchesWhenObscured(true);

        actions.addView(openButton, new LinearLayout.LayoutParams(0, dp(50), 1f));
        actions.addView(deleteButton, new LinearLayout.LayoutParams(0, dp(50), 1f));
        page.addView(actions);

        final int[] selectedIndex = {-1};

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

        addButton.setOnClickListener(v -> {
            String newSubject = subjectInput.getText().toString().trim();
            if (newSubject.isEmpty()) {
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
                subjects.remove(selectedIndex[0]);
                subjectAdapter.notifyDataSetChanged();
                selectedIndex[0] = -1;
                openButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        });

        showInContent(page);
    }

    private void showSubjectPage(String subject) {
        LinearLayout page = createVerticalPage();

        Button backButton = new Button(this);
        backButton.setText("← Zurück zu den Fächern");
        backButton.setAllCaps(false);
        backButton.setOnClickListener(v -> showGradesPage());
        page.addView(backButton);

        page.addView(createTitle(subject));
        page.addView(createSubtitle("Klassenstufe " + selectedClass));

        LinearLayout writtenCard = createCard();
        writtenCard.addView(createText(selectedClass >= 11 ? "Klausuren" : "Klassenarbeiten", 19, true));
        writtenCard.addView(createText("Hier können später schriftliche Noten eingetragen werden.", 15, false));
        Button writtenButton = new Button(this);
        writtenButton.setText("+ Eintrag hinzufügen");
        writtenButton.setAllCaps(false);
        writtenButton.setEnabled(false);
        writtenCard.addView(writtenButton);

        LinearLayout oralCard = createCard();
        oralCard.addView(createText("Mündliche Noten", 19, true));
        oralCard.addView(createText("Hier können später mündliche Bewertungen eingetragen werden.", 15, false));
        Button oralButton = new Button(this);
        oralButton.setText("+ Eintrag hinzufügen");
        oralButton.setAllCaps(false);
        oralButton.setEnabled(false);
        oralCard.addView(oralButton);

        page.addView(writtenCard);
        page.addView(oralCard);

        showInContent(wrapInScrollView(page));
    }

    private void showFinancePage() {
        LinearLayout page = createVerticalPage();
        page.addView(createTitle("Finanzen"));
        page.addView(createSubtitle("Der Finanzbereich bleibt vorerst frei und wird später aufgebaut."));

        LinearLayout card = createCard();
        card.addView(createText("Geplant", 19, true));
        card.addView(createText("Ausgaben nach Tag, Woche und Monat verwalten.", 15, false));
        page.addView(card);

        showInContent(wrapInScrollView(page));
    }

    private void showPrivacyPage() {
        LinearLayout page = createVerticalPage();

        Button backButton = new Button(this);
        backButton.setText("← Zurück");
        backButton.setAllCaps(false);
        backButton.setOnClickListener(v -> showHomePage());
        page.addView(backButton);

        page.addView(createTitle("Datenschutz & Sicherheit"));

        LinearLayout privacyCard = createCard();
        privacyCard.addView(createText("Aktueller Datenschutzstatus", 19, true));
        privacyCard.addView(createText(
                "• Die App fordert keine Standort-, Kamera-, Mikrofon-, Kontakt- oder Speicherberechtigungen an.\n" +
                "• Die App enthält aktuell keine Werbung, Analyse- oder Tracking-SDKs.\n" +
                "• Die App besitzt aktuell keine Internetberechtigung und überträgt keine eingegebenen Daten.\n" +
                "• Unverschlüsselte Netzwerkverbindungen (HTTP) sind zusätzlich technisch gesperrt.\n" +
                "• Android-Cloud-Backups sind deaktiviert, damit Schul- oder Finanzdaten später nicht unbeabsichtigt gesichert werden.\n" +
                "• Es gibt aktuell kein Benutzerkonto.",
                15,
                false
        ));

        LinearLayout controlCard = createCard();
        controlCard.addView(createText("Deine Kontrolle", 19, true));
        controlCard.addView(createText(
                "Du kannst alle aktuell in dieser Sitzung angelegten Fächer löschen. Sobald dauerhafte Speicherung eingebaut wird, wird diese Funktion entsprechend erweitert.",
                15,
                false
        ));

        Button clearButton = new Button(this);
        clearButton.setText("Lokale Daten dieser Sitzung löschen");
        clearButton.setAllCaps(false);
        clearButton.setFilterTouchesWhenObscured(true);
        clearButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Daten löschen?")
                .setMessage("Alle aktuell angelegten Fächer werden aus dieser Sitzung entfernt.")
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (dialog, which) -> {
                    subjectsByClass.clear();
                    Toast.makeText(this, "Lokale Sitzungsdaten gelöscht.", Toast.LENGTH_SHORT).show();
                    showPrivacyPage();
                })
                .show());

        controlCard.addView(clearButton);

        page.addView(privacyCard);
        page.addView(controlCard);

        showInContent(wrapInScrollView(page));
    }

    private ArrayList<String> getSubjectsForClass(int classLevel) {
        if (!subjectsByClass.containsKey(classLevel)) {
            subjectsByClass.put(classLevel, new ArrayList<>());
        }
        return subjectsByClass.get(classLevel);
    }

    private LinearLayout createVerticalPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(20), dp(20), dp(20));
        page.setBackgroundColor(Color.rgb(245, 245, 245));
        return page;
    }

    private TextView createTitle(String text) {
        TextView title = createText(text, 30, true);
        title.setPadding(0, 0, 0, dp(8));
        return title;
    }

    private TextView createSubtitle(String text) {
        TextView subtitle = createText(text, 16, false);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, 0, 0, dp(12));
        return subtitle;
    }

    private TextView createText(String text, float size, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(Color.BLACK);
        if (bold) {
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        }
        textView.setPadding(0, dp(4), 0, dp(4));
        return textView;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(12));
        background.setStroke(dp(1), Color.rgb(220, 220, 220));
        card.setBackground(background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(8));
        card.setLayoutParams(params);
        return card;
    }

    private ScrollView wrapInScrollView(View child) {
        ScrollView scrollView = new ScrollView(this);
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
