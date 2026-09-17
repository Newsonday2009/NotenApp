import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class NotenApp extends Application {

    private BorderPane root;

    // Aktuell ausgewählte Klassenstufe
    private int selectedClass = 12;

    // Für jede Klassenstufe gibt es eine eigene Fächerliste.
    // Später kann diese Struktur durch eine Datenbank ersetzt werden.
    private final Map<Integer, ObservableList<String>> subjectsByClass = new HashMap<>();

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        createNavigation();
        showStartPage();

        Scene scene = new Scene(root, 430, 700);

        stage.setTitle("NotenApp");
        stage.setScene(scene);
        stage.setMinWidth(380);
        stage.setMinHeight(600);
        stage.show();
    }

    private void createNavigation() {
        Button notenButton = new Button("Noten");
        Button homeButton = new Button("Home");
        Button finanzenButton = new Button("Finanzen");

        styleNavigationButton(notenButton);
        styleNavigationButton(homeButton);
        styleNavigationButton(finanzenButton);

        notenButton.setOnAction(e -> showNotenPage());
        homeButton.setOnAction(e -> showStartPage());
        finanzenButton.setOnAction(e -> showFinanzenPage());

        HBox navigation = new HBox(10, notenButton, homeButton, finanzenButton);
        navigation.setAlignment(Pos.CENTER);
        navigation.setPadding(new Insets(12));
        navigation.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 1 0 0 0;");

        HBox.setHgrow(notenButton, Priority.ALWAYS);
        HBox.setHgrow(homeButton, Priority.ALWAYS);
        HBox.setHgrow(finanzenButton, Priority.ALWAYS);

        root.setBottom(navigation);
    }

    private void styleNavigationButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: #eeeeee;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 14px;"
        );
    }

    // --------------------------------------------------
    // STARTSEITE
    // --------------------------------------------------

    private void showStartPage() {
        VBox page = createPageContainer();

        Label title = createTitle("Startseite");
        Label subtitle = new Label("Deine Schulübersicht");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

        VBox schoolCard = createCard();
        Label classLabel = new Label("Klassenstufe: " + selectedClass);
        classLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        int subjectCount = getSubjectsForClass(selectedClass).size();
        Label subjectCountLabel = new Label("Angelegte Fächer: " + subjectCount);
        subjectCountLabel.setStyle("-fx-font-size: 15px;");

        Button openGradesButton = new Button("Zu meinen Fächern");
        openGradesButton.setMaxWidth(Double.MAX_VALUE);
        openGradesButton.setPrefHeight(42);
        openGradesButton.setOnAction(e -> showNotenPage());

        schoolCard.getChildren().addAll(classLabel, subjectCountLabel, openGradesButton);

        VBox financeCard = createCard();
        Label financeTitle = new Label("Finanzen");
        financeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label financeInfo = new Label("Dieser Bereich wird später ergänzt.");
        financeInfo.setStyle("-fx-text-fill: #666666;");
        financeCard.getChildren().addAll(financeTitle, financeInfo);

        page.getChildren().addAll(title, subtitle, schoolCard, financeCard);
        root.setCenter(page);
    }

    // --------------------------------------------------
    // NOTEN / FÄCHER
    // --------------------------------------------------

    private void showNotenPage() {
        VBox page = createPageContainer();

        Label title = createTitle("Noten");

        Label classLabel = new Label("Klassenstufe");
        classLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<Integer> classSelector = new ComboBox<>();
        for (int i = 5; i <= 13; i++) {
            classSelector.getItems().add(i);
        }
        classSelector.setValue(selectedClass);
        classSelector.setMaxWidth(Double.MAX_VALUE);

        Label subjectLabel = new Label("Fächer");
        subjectLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> subjectList = new ListView<>();
        subjectList.setItems(getSubjectsForClass(selectedClass));
        subjectList.setPrefHeight(300);
        VBox.setVgrow(subjectList, Priority.ALWAYS);

        TextField subjectInput = new TextField();
        subjectInput.setPromptText("Neues Fach, z. B. Mathematik");

        Button addButton = new Button("Fach hinzufügen");
        addButton.setMaxWidth(Double.MAX_VALUE);

        Button openButton = new Button("Fach öffnen");
        openButton.setDisable(true);

        Button deleteButton = new Button("Löschen");
        deleteButton.setDisable(true);

        HBox subjectActions = new HBox(10, openButton, deleteButton);
        HBox.setHgrow(openButton, Priority.ALWAYS);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);
        openButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        classSelector.setOnAction(e -> {
            selectedClass = classSelector.getValue();
            subjectList.setItems(getSubjectsForClass(selectedClass));
            openButton.setDisable(true);
            deleteButton.setDisable(true);
        });

        addButton.setOnAction(e -> {
            String newSubject = subjectInput.getText().trim();

            if (newSubject.isEmpty()) {
                return;
            }

            ObservableList<String> subjects = getSubjectsForClass(selectedClass);

            boolean alreadyExists = subjects.stream()
                    .anyMatch(subject -> subject.equalsIgnoreCase(newSubject));

            if (!alreadyExists) {
                subjects.add(newSubject);
                subjectInput.clear();
            } else {
                showInformation("Dieses Fach ist bereits vorhanden.");
            }
        });

        subjectInput.setOnAction(e -> addButton.fire());

        subjectList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            boolean nothingSelected = newValue == null;
            openButton.setDisable(nothingSelected);
            deleteButton.setDisable(nothingSelected);
        });

        openButton.setOnAction(e -> {
            String selectedSubject = subjectList.getSelectionModel().getSelectedItem();
            if (selectedSubject != null) {
                showSubjectPage(selectedSubject);
            }
        });

        subjectList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                String selectedSubject = subjectList.getSelectionModel().getSelectedItem();
                if (selectedSubject != null) {
                    showSubjectPage(selectedSubject);
                }
            }
        });

        deleteButton.setOnAction(e -> {
            String selectedSubject = subjectList.getSelectionModel().getSelectedItem();
            if (selectedSubject != null) {
                getSubjectsForClass(selectedClass).remove(selectedSubject);
            }
        });

        page.getChildren().addAll(
                title,
                classLabel,
                classSelector,
                createSpacer(5),
                subjectLabel,
                subjectList,
                subjectInput,
                addButton,
                subjectActions
        );

        root.setCenter(page);
    }

    // --------------------------------------------------
    // EINZELNES FACH
    // --------------------------------------------------

    private void showSubjectPage(String subject) {
        VBox page = createPageContainer();

        Button backButton = new Button("← Zurück zu den Fächern");
        backButton.setOnAction(e -> showNotenPage());

        Label title = createTitle(subject);
        Label classInfo = new Label("Klassenstufe " + selectedClass);
        classInfo.setStyle("-fx-font-size: 15px; -fx-text-fill: #666666;");

        VBox writtenCard = createCard();
        Label writtenTitle = new Label(selectedClass >= 11 ? "Klausuren" : "Klassenarbeiten");
        writtenTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label writtenInfo = new Label("Hier können später schriftliche Noten eingetragen werden.");
        writtenInfo.setWrapText(true);
        writtenInfo.setStyle("-fx-text-fill: #666666;");
        Button writtenPlaceholderButton = new Button("+ Eintrag hinzufügen");
        writtenPlaceholderButton.setDisable(true);
        writtenCard.getChildren().addAll(writtenTitle, writtenInfo, writtenPlaceholderButton);

        VBox oralCard = createCard();
        Label oralTitle = new Label("Mündliche Noten");
        oralTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label oralInfo = new Label("Hier können später mündliche Bewertungen eingetragen werden.");
        oralInfo.setWrapText(true);
        oralInfo.setStyle("-fx-text-fill: #666666;");
        Button oralPlaceholderButton = new Button("+ Eintrag hinzufügen");
        oralPlaceholderButton.setDisable(true);
        oralCard.getChildren().addAll(oralTitle, oralInfo, oralPlaceholderButton);

        page.getChildren().addAll(backButton, title, classInfo, writtenCard, oralCard);
        root.setCenter(page);
    }

    // --------------------------------------------------
    // FINANZEN
    // --------------------------------------------------

    private void showFinanzenPage() {
        VBox page = createPageContainer();

        Label title = createTitle("Finanzen");
        Label info = new Label("Der Finanzbereich bleibt vorerst frei und wird später aufgebaut.");
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

        VBox placeholder = createCard();
        Label placeholderTitle = new Label("Geplant");
        placeholderTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label placeholderText = new Label("Ausgaben nach Tag, Woche und Monat verwalten.");
        placeholderText.setWrapText(true);
        placeholder.getChildren().addAll(placeholderTitle, placeholderText);

        page.getChildren().addAll(title, info, placeholder);
        root.setCenter(page);
    }

    // --------------------------------------------------
    // HILFSMETHODEN
    // --------------------------------------------------

    private ObservableList<String> getSubjectsForClass(int classLevel) {
        return subjectsByClass.computeIfAbsent(classLevel, key -> FXCollections.observableArrayList());
    }

    private VBox createPageContainer() {
        VBox page = new VBox(14);
        page.setPadding(new Insets(24));
        page.setAlignment(Pos.TOP_LEFT);
        return page;
    }

    private Label createTitle(String text) {
        Label title = new Label(text);
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        return title;
    }

    private VBox createCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 10;"
        );
        return card;
    }

    private Region createSpacer(double height) {
        Region spacer = new Region();
        spacer.setPrefHeight(height);
        return spacer;
    }

    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hinweis");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
