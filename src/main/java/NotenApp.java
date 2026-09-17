import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NotenApp extends Application {

    private BorderPane root;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();

        showStartPage();

        Button notenButton = new Button("Noten");
        Button homeButton = new Button("Home");
        Button finanzenButton = new Button("Finanzen");

        notenButton.setOnAction(e -> showNotenPage());
        homeButton.setOnAction(e -> showStartPage());
        finanzenButton.setOnAction(e -> showFinanzenPage());

        HBox navigation = new HBox(40);
        navigation.setAlignment(Pos.CENTER);
        navigation.setPadding(new Insets(15));
        navigation.getChildren().addAll(notenButton, homeButton, finanzenButton);

        root.setBottom(navigation);

        Scene scene = new Scene(root, 430, 700);
        stage.setTitle("NotenApp");
        stage.setScene(scene);
        stage.show();
    }

    private void showStartPage() {
        VBox page = new VBox();
        page.setAlignment(Pos.CENTER);

        Label title = new Label("Startseite");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        page.getChildren().add(title);
        root.setCenter(page);
    }

    private void showNotenPage() {
        VBox page = new VBox();
        page.setAlignment(Pos.CENTER);

        Label title = new Label("Noten");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        page.getChildren().add(title);
        root.setCenter(page);
    }

    private void showFinanzenPage() {
        VBox page = new VBox();
        page.setAlignment(Pos.CENTER);

        Label title = new Label("Finanzen");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        page.getChildren().add(title);
        root.setCenter(page);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
