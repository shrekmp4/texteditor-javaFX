package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

public class TextEditor extends Application {

    private TextArea textArea;
    private File currentFile;
    private Label statusLabel;
    private ChoiceBox<String> fontChoiceBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Editor de Texto Simple");

        textArea = new TextArea();
        textArea.textProperty().addListener((observable, oldValue, newValue) -> updateStatus());

        BorderPane root = new BorderPane(textArea);

        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        updateStatus(); // Inicializar el estado
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Archivo");
        MenuItem newMenuItem = new MenuItem("Nuevo CTRL + N");
        MenuItem openMenuItem = new MenuItem("Abrir CTRL + O");
        MenuItem saveMenuItem = new MenuItem("Guardar CTRL + S");
        MenuItem saveAsMenuItem = new MenuItem("Guardar Como");
        MenuItem exitMenuItem = new MenuItem("Salir CTRL + Q");

        fileMenu.getItems().addAll(newMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem, new SeparatorMenuItem(), exitMenuItem);

        Menu settingsMenu = new Menu("Configuración");
        MenuItem preferencesMenuItem = new MenuItem("Preferencias");
        settingsMenu.getItems().add(preferencesMenuItem);

        Menu fontMenu = new Menu("Fuente");
        fontChoiceBox = new ChoiceBox<>();
        fontChoiceBox.getItems().addAll(Font.getFontNames());
        fontChoiceBox.setValue(textArea.getFont().getFamily()); // Establecer el valor inicial
        fontMenu.getItems().add(new CustomMenuItem(fontChoiceBox));

        menuBar.getMenus().addAll(fileMenu, settingsMenu, fontMenu);

        newMenuItem.setOnAction(event -> newFile());
        openMenuItem.setOnAction(event -> openFile());
        saveMenuItem.setOnAction(event -> saveFile());
        saveAsMenuItem.setOnAction(event -> saveFileAs());
        exitMenuItem.setOnAction(event -> System.exit(0));

        preferencesMenuItem.setOnAction(event -> showPreferencesDialog());
        fontChoiceBox.setOnAction(event -> changeFont(fontChoiceBox.getValue()));

        return menuBar;
    }

    private void newFile() {
        textArea.clear();
        currentFile = null;
        updateStatus();
    }

    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                textArea.setText(content);
                currentFile = selectedFile;
                updateStatus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            saveToFile(currentFile);
        } else {
            saveFileAs();
        }
    }

    private void saveFileAs() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            saveToFile(selectedFile);
            currentFile = selectedFile;
            updateStatus();
        }
    }

    private void saveToFile(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(textArea.getText());
            fileWriter.close();
            updateStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus() {
        int numLines = textArea.getText().split("\n").length;
        int numCharacters = textArea.getText().length();
        String encoding = System.getProperty("file.encoding");
        long fileSize = (currentFile != null) ? currentFile.length() : 0;
        String fontName = textArea.getFont().getFamily();

        String statusText = String.format("Líneas: %d, Caracteres: %d, Codificación: %s, Tamaño de Archivo: %d bytes, Fuente: %s", numLines, numCharacters, encoding, fileSize, fontName);
        statusLabel.setText(statusText);
    }

    private void changeFont(String fontName) {
        textArea.setFont(Font.font(fontName, textArea.getFont().getSize()));
        updateStatus();
    }

    private void showPreferencesDialog() {
        ChoiceDialog<String> languageDialog = new ChoiceDialog<>("Inglés", "Inglés", "Español");
        languageDialog.setTitle("Preferencias");
        languageDialog.setHeaderText("Elija el Idioma:");
        languageDialog.setContentText("Idioma:");

        languageDialog.showAndWait().ifPresent(result -> {
            if (result.equals("Español")) {
                // Cambiar el idioma a español
                Locale newLocale = new Locale.Builder().setLanguage("es").setRegion("ES").build();
                Locale.setDefault(newLocale);
                updateLocalizedMenus();
            } else {
                // Cambiar el idioma a inglés
                Locale.setDefault(Locale.ENGLISH);
                updateLocalizedMenus();
            }
        });
    }

    private void updateLocalizedMenus() {
        MenuBar menuBar = createMenuBar();
        BorderPane root = (BorderPane) textArea.getParent();
        root.setTop(menuBar);
    }
}
