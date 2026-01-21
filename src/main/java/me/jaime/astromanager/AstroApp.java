package me.jaime.astromanager;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import me.jaime.astromanager.objects.CelestialBody;
import org.controlsfx.control.RangeSlider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class AstroApp extends Application {

    // VARIABLES GLOBALES
    private Slider slider;
    private Slider minHeightSlider;
    private RangeSlider azimuthRange;

    @Override
    public void start(Stage stage) {

        VBox rootConfig = new VBox(20);
        rootConfig.setAlignment(Pos.CENTER);
        rootConfig.setPadding(new Insets(40));
        rootConfig.setStyle("-fx-background-color: #1a1a1a;");

        // --- TITULO ---
        Label titleLabel = new Label("Welcome to AstroManager");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // --- LATITUD (DMS) ---
        Label latLabel = new Label("Enter Latitude (South negative -):");
        latLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");

        // Creamos las 3 cajitas para Latitud
        TextField latDeg = createSmallField("00");
        TextField latMin = createSmallField("00");
        TextField latSec = createSmallField("00");

        // Las metemos en una caja horizontal bonita
        HBox latBox = createDMSBox(latDeg, latMin, latSec);

        // --- LONGITUD (DMS) ---
        Label longLabel = new Label("Enter Longitude (West negative -):");
        longLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");

        // Creamos las 3 cajitas para Longitud
        TextField lonDeg = createSmallField("00");
        TextField lonMin = createSmallField("00");
        TextField lonSec = createSmallField("00");

        HBox longBox = createDMSBox(lonDeg, lonMin, lonSec);

        // --- FECHA ---
        Label dateLabel = new Label("Select observation date:");
        dateLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-control-inner-background: #333333;");

        // --- BOTÓN START ---
        Button enterButton = new Button("Start Observation");
        styleButton(enterButton);

        enterButton.setOnAction(e -> {
            try {
                // AQUÍ ESTÁ LA MAGIA: Convertimos DMS a Decimal
                double userLat = dmsToDecimal(latDeg, latMin, latSec);
                double userLong = dmsToDecimal(lonDeg, lonMin, lonSec);

                LocalDate userDate = datePicker.getValue();
                LocalDateTime userTime = LocalDateTime.now();

                startMainApplication(stage, userLat, userLong, userDate, userTime);

            } catch (NumberFormatException error) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Incorrect data");
                alert.setContentText("Please verify that all fields contain numbers.");
                alert.showAndWait();
            }
        });

        rootConfig.getChildren().addAll(titleLabel, latLabel, latBox, longLabel, longBox, dateLabel, datePicker, enterButton);

        Scene configScene = new Scene(rootConfig, 500, 600); // Un poco más alto
        stage.setTitle("AstroManager Configuration");
        stage.setScene(configScene);
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/logo.png")).toExternalForm()));
        } catch (Exception ex) { System.out.println("Logo not found"); }
        stage.show();
    }

    // --- MÉTODOS AUXILIARES NUEVOS (Cópialos abajo en tu clase) ---

    // 1. Crea una cajita de texto pequeña y estilizada
    private TextField createSmallField(String prompt) {
        TextField tf = new TextField(prompt);
        tf.setPrefWidth(60); // Ancho pequeño
        tf.setAlignment(Pos.CENTER); // Texto centrado
        tf.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 3;");
        return tf;
    }

    // 2. Crea el diseño visual: [ ] ° [ ] ' [ ] ''
    private HBox createDMSBox(TextField deg, TextField min, TextField sec) {
        Label l1 = new Label("°"); l1.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        Label l2 = new Label("'"); l2.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        Label l3 = new Label("''"); l3.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        HBox box = new HBox(10, deg, l1, min, l2, sec, l3);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    // 3. LA MATEMÁTICA: Convierte Grados/Min/Seg a Decimal
    private double dmsToDecimal(TextField d, TextField m, TextField s) {
        // Leemos texto, si está vacío asumimos 0
        double deg = d.getText().isEmpty() ? 0 : Double.parseDouble(d.getText());
        double min = m.getText().isEmpty() ? 0 : Double.parseDouble(m.getText());
        double sec = s.getText().isEmpty() ? 0 : Double.parseDouble(s.getText());

        // Manejo de negativos (ej: -3 grados 30 minutos)
        // Si los grados son negativos, los minutos y segundos deben restar también
        double sign = (deg < 0) ? -1.0 : 1.0;

        // Fórmula: Grados + (Minutos/60) + (Segundos/3600)
        // Usamos Valor Absoluto en deg para sumar todo y luego aplicar el signo al final
        return sign * (Math.abs(deg) + (min / 60.0) + (sec / 3600.0));
    }

    private void startMainApplication(Stage stage, double userLat, double userLong, LocalDate observationDate, LocalDateTime userTime) {

        // --- CARGA DE DATOS ---
        ArrayList<CelestialBody> list = AstroLoader.loadObjects("objects.csv");
        ObservableList<CelestialBody> observableList = FXCollections.observableList(list);
        FilteredList<CelestialBody> filteredList = new FilteredList<>(observableList, p -> true);
        SortedList<CelestialBody> sortedList = new SortedList<>(filteredList);

        // --- CÁLCULO HORA INICIAL ---
        int hour = userTime.getHour();
        int minutes = userTime.getMinute();
        double sliderValue = hour + (minutes / 60.0);
        if (sliderValue < 12) sliderValue += 24;
        if (sliderValue < 18 || sliderValue > 30) { sliderValue = 18; hour = 18; minutes = 0; }

        // --- SLIDER DE TIEMPO ---
        slider = new Slider(18, 30, sliderValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        // IMPORTANTE: Quitamos MaxWidth fijo, usamos prefWidth para que se adapte
        slider.setPrefWidth(800);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(3);

        slider.setLabelFormatter(new StringConverter<>() {
            @Override public String toString(Double value) {
                if (value == 24) return "00:00";
                if (value > 24) return String.format("0%.0f:00", value - 24);
                return String.format("%.0f:00", value);
            }
            @Override public Double fromString(String string) { return 0.0; }
        });

        // --- ETIQUETA DE TIEMPO (ANTI-BAILE 1) ---
        Label timeLabel = new Label(String.format("Time: %02dh %02dmin", hour, minutes));
        timeLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px");
        // TRUCO: Le damos un ancho fijo y centramos el texto dentro.
        // Así, aunque cambien los números, la caja mide siempre lo mismo.
        timeLabel.setPrefWidth(300);
        timeLabel.setAlignment(Pos.CENTER);

        // --- TABLA ---
        TableView<CelestialBody> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Las columnas se reparten el espacio

        // COLUMNAS (Porcentajes)
        TableColumn<CelestialBody, String> nameCol = new TableColumn<>("Name");
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.25)); // 25%
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CelestialBody, String> typeCol = new TableColumn<>("Type");
        typeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12)); // 12%
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeBody"));
        typeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CelestialBody, Double> magCol = new TableColumn<>("Mag");
        magCol.prefWidthProperty().bind(table.widthProperty().multiply(0.08)); // 8%
        magCol.setCellValueFactory(new PropertyValueFactory<>("magnitude"));
        magCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CelestialBody, String> heightCol = new TableColumn<>("Height / State");
        heightCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20)); // 20%
        heightCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CelestialBody, String> azimuthCol = new TableColumn<>("Azimuth");
        azimuthCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15)); // 15%
        azimuthCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CelestialBody, String> timeLeftCol = new TableColumn<>("Time Left");
        timeLeftCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20)); // 20%
        timeLeftCol.setStyle("-fx-alignment: CENTER;");

        // --- ETIQUETA ALTURA (ANTI-BAILE 2) ---
        Label lblMinHeight = new Label("Min Height: 30°");
        lblMinHeight.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px;");
        lblMinHeight.setPrefWidth(200); // Ancho fijo para que no empuje
        lblMinHeight.setAlignment(Pos.CENTER);

        // SLIDER ALTURA
        minHeightSlider = new Slider(0, 90, 30);
        minHeightSlider.setShowTickLabels(true);
        minHeightSlider.setShowTickMarks(true);
        minHeightSlider.setMajorTickUnit(10);
        minHeightSlider.setPrefWidth(400); // Tamaño razonable

        minHeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMinHeight.setText(String.format("Min Height: %.0f°", newVal.doubleValue()));
            updateSorting(sortedList, table, userLat, userLong, observationDate);
            table.refresh();
        });

        // --- ETIQUETA AZIMUT (ANTI-BAILE 3) ---
        Label lblAzimuth = new Label("Blocked Range: None");
        lblAzimuth.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px");
        lblAzimuth.setPrefWidth(300); // Ancho fijo
        lblAzimuth.setAlignment(Pos.CENTER);

        // SLIDER AZIMUT
        azimuthRange = new RangeSlider(0, 360, 0, 50);
        azimuthRange.setShowTickLabels(true);
        azimuthRange.setShowTickMarks(true);
        azimuthRange.setMajorTickUnit(45);
        azimuthRange.setPrefWidth(400); // Igual que el de altura
        updateAzimuthLabel(lblAzimuth, azimuthRange);

        azimuthRange.lowValueProperty().addListener((a, b, c) -> {
            updateAzimuthLabel(lblAzimuth, azimuthRange);
            updateSorting(sortedList, table, userLat, userLong, observationDate);
            table.refresh();
        });
        azimuthRange.highValueProperty().addListener((a, b, c) -> {
            updateAzimuthLabel(lblAzimuth, azimuthRange);
            updateSorting(sortedList, table, userLat, userLong, observationDate);
            table.refresh();
        });

        VBox azimuthBox = new VBox(5, lblAzimuth, azimuthRange);
        azimuthBox.setAlignment(Pos.CENTER);

        // --- DEFINICIÓN DE CELDAS (LÓGICA) ---
        // (Copia aquí tus setCellValueFactory de heightCol, azimuthCol y timeLeftCol tal cual los tenías)
        // ... [TU CÓDIGO DE CELDAS VA AQUÍ] ...

        // Configuración HeightCol para que funcione al copiar:
        heightCol.setCellValueFactory(cell -> {
            CelestialBody object = cell.getValue();
            double limit = minHeightSlider.getValue();
            double time = slider.getValue();
            double lstHours = AstroCalculator.calculateLST(observationDate, time, userLong);
            double height = AstroCalculator.calculateCurrentHeight(object, userLat, lstHours * 15.0);
            double starAzimuth = AstroCalculator.getAzimuth(object, userLat, lstHours);

            double minBlock = azimuthRange.getLowValue();
            double maxBlock = azimuthRange.getHighValue();
            boolean isBlockedByWall = (starAzimuth >= minBlock && starAzimuth <= maxBlock);

            String state;
            if (height < limit) state = "🌑 Hidden";
            else if (height < (limit + 10)) state = "⚠ Low (" + String.format("%.0f", height) + "°)";
            else if (isBlockedByWall) state = "⛔ Blocked";
            else state = "✅ Visible (" + String.format("%.0f", height) + "°)";

            return new SimpleStringProperty(state);
        });

        azimuthCol.setCellValueFactory(cell -> {
            CelestialBody object = cell.getValue();
            double time = slider.getValue();
            double lstHours = AstroCalculator.calculateLST(observationDate, time, userLong);
            double azDegrees = AstroCalculator.getAzimuth(object, userLat, lstHours);
            String direction = getCompassDirection(azDegrees);
            return new SimpleStringProperty(String.format("%.0f° (%s)", azDegrees, direction));
        });

        timeLeftCol.setCellValueFactory(cell -> {
            CelestialBody object = cell.getValue();
            double time = slider.getValue();
            double startBlock = azimuthRange.getLowValue();
            double endBlock = azimuthRange.getHighValue();

            String timeLeft = AstroCalculator.calculateTimeLeft(object, userLat, userLong, time, observationDate, minHeightSlider.getValue(), startBlock, endBlock);
            return new SimpleStringProperty(timeLeft);
        });

        // --- CONFIGURACIÓN DE TABLA ---
        table.setRowFactory(tv -> {
            TableRow<CelestialBody> row = new TableRow<>();
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && !row.isEmpty()) {
                    if (row.isSelected()) {
                        table.getSelectionModel().clearSelection();
                        event.consume();
                    }
                }
            });
            return row;
        });

        table.setItems(sortedList);
        table.getColumns().addAll(nameCol, typeCol, magCol, heightCol, azimuthCol, timeLeftCol);

        // --- LAYOUT CORREGIDO (AQUÍ ESTÁ LA MAGIA) ---

        // 1. Contenedor de la tabla
        HBox tableContainer = new HBox(table);
        tableContainer.setAlignment(Pos.TOP_CENTER);
        // IMPORTANTE: Permitir que la tabla crezca horizontalmente TODO lo que pueda
        HBox.setHgrow(table, Priority.ALWAYS);
        // IMPORTANTE: Permitir que el contenedor crezca verticalmente
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // IMPORTANTE: ¡QUITAR LÍMITES FIJOS!
        table.setMaxWidth(Double.MAX_VALUE); // Libertad total
        table.setMaxHeight(Double.MAX_VALUE);

        // 2. Panel Izquierdo (Filtros)
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search object...");
        searchField.setMaxWidth(Double.MAX_VALUE); // Que ocupe el ancho del panel izquierdo
        searchField.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-prompt-text-fill: gray; -fx-border-color: #555555; -fx-border-radius: 5; -fx-font-size: 16px;");

        // Listener del buscador... (tu código)
        searchField.textProperty().addListener((a, b, newValue) -> {
            filteredList.setPredicate(body -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return body.getName().toLowerCase().contains(lower) || body.getCatalogId().toLowerCase().contains(lower);
            });
            table.refresh();
        });

        VBox heightFilterBox = new VBox(15, lblMinHeight, minHeightSlider);
        heightFilterBox.setAlignment(Pos.CENTER);

        VBox filters = new VBox(40, searchField, heightFilterBox, azimuthBox);
        filters.setMinWidth(300); // Ancho mínimo para los filtros
        filters.setPrefWidth(350); // Ancho preferido
        filters.setMaxWidth(400); // Que no se hagan gigantes

        // 3. Contenedor Principal (Filtros + Tabla)
        HBox content = new HBox(30);
        content.getChildren().addAll(filters, tableContainer);
        // Decimos: "Si sobra espacio horizontal, dáselo TODO al contenedor de la tabla"
        HBox.setHgrow(tableContainer, Priority.ALWAYS);

        // 4. Header (Título y Botones)
        updateSorting(sortedList, table, userLat, userLong, observationDate);

        slider.valueProperty().addListener((a, b, newVal) -> {
            double value = newVal.doubleValue();
            double visualHour = value >= 24 ? value - 24 : value;
            int hours = (int) visualHour;
            int minute = (int) ((visualHour - hours) * 60);
            timeLabel.setText(String.format("Time: %02dh %02dmin", hours, minute));
            updateSorting(sortedList, table, userLat, userLong, observationDate);
        });

        HBox timeButtonsContainer = new HBox(30);
        timeButtonsContainer.setAlignment(Pos.CENTER);
        timeButtonsContainer.getChildren().addAll(timeLabel,
                createButton("19:30H", 19.5, slider),
                createButton("21:00H", 21.0, slider),
                createButton("22:30H", 22.5, slider),
                createButton("00:00H", 24.0, slider),
                createButton("01:30H", 25.5, slider),
                createButton("03:00H", 27.0, slider)
        );

        VBox timeControls = new VBox(10, timeButtonsContainer, slider);
        timeControls.setAlignment(Pos.CENTER);

        // 5. Ensamblaje Final
        VBox vbox = new VBox(20); // Reducimos el espacio fijo entre elementos
        vbox.setPadding(new Insets(30, 20, 20, 20)); // (Arriba, Der, Aba, Izq) -> 30px arriba para separar del borde
        vbox.setStyle("-fx-background-color: #1a1a1a;");

        DateTimeFormatter baseFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
        Label title = new Label("🔭 AstroManager - " + observationDate.format(baseFormatter) + " - By Jaime Morales");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // CAMBIO 1: Alineamos arriba en vez de al centro
        vbox.setAlignment(Pos.TOP_CENTER);

        vbox.getChildren().addAll(title, timeControls, content);

        // CAMBIO 2: ¡La clave! Decimos que 'content' (la tabla y filtros) crezca verticalmente
        // para ocupar todo el espacio negro vacío de abajo.
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene mainScene = new Scene(vbox, 1280, 720);
        try { mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm()); }
        catch (Exception e) { System.out.println("⚠ styles.css not found"); }

        stage.setTitle("AstroManager - Lat: " + userLat + " Long: " + userLong);
        stage.setScene(mainScene);
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    private String getCompassDirection(double azimuth) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        // Fórmula mágica para convertir 0-360 en índice 0-8
        int index = (int) Math.round(((azimuth % 360) / 45));
        return directions[index];
    }

    private Button createButton(String text, double value, Slider slider) {
        Button b = new Button(text);
        b.setOnAction(a -> slider.setValue(value));
        styleButton(b);
        return b;
    }

    private void styleButton(Button b) {
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #00d2ff;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;" +
                        "-fx-text-fill: #00d2ff;" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 15 5 15;" +
                        "-fx-cursor: hand;"
        );
    }

    // --- MÉTODOS DE ORDENACIÓN ARREGLADOS ---

    private void updateSorting(
            SortedList<CelestialBody> sortedList,
            TableView table,
            double userLat,
            double userLong,
            LocalDate observationDate) {

        sortedList.setComparator((o1, o2) -> {
            // Ahora 'slider' (el de tiempo) es accesible globalmente
            double time = slider.getValue();
            double lstHours = AstroCalculator.calculateLST(observationDate, time, userLong);

            // --- CÁLCULOS OBJETO 1 ---
            double alt1 = AstroCalculator.calculateCurrentHeight(o1, userLat, lstHours * 15.0);
            double az1 = AstroCalculator.getAzimuth(o1, userLat, lstHours);
            int rank1 = calculateRank(alt1, az1);

            // --- CÁLCULOS OBJETO 2 ---
            double alt2 = AstroCalculator.calculateCurrentHeight(o2, userLat, lstHours * 15.0);
            double az2 = AstroCalculator.getAzimuth(o2, userLat, lstHours);
            int rank2 = calculateRank(alt2, az2);

            // --- COMPARACIÓN ---

            // 1. PRIMER CRITERIO: El Rango (Visible gana a Blocked, etc.)
            if (rank1 != rank2) {
                return Integer.compare(rank1, rank2); // Menor rango gana (1 gana a 2)
            }

            // 2. SEGUNDO CRITERIO: Si empatan en rango, gana el más alto
            return Double.compare(alt2, alt1);
        });

        table.refresh();
    }

    private int calculateRank(double altitude, double azimuth) {
        double minHeight = minHeightSlider.getValue();

        double blockStart = azimuthRange.getLowValue();
        double blockEnd = azimuthRange.getHighValue();
        boolean isBlocked = (azimuth >= blockStart && azimuth <= blockEnd);

        // 1️⃣ HIDDEN
        if (altitude < minHeight) {
            return 4;
        }

        // 2️⃣ BLOCKED (altura buena, pero obstáculo)
        if (isBlocked && altitude >= (minHeight + 10)) {
            return 2;
        }

        // 3️⃣ LOW (visible pero muy bajo)
        if (altitude < (minHeight + 10)) {
            return 3;
        }

        // 4️⃣ VISIBLE
        return 1;
    }


    public static void main(String[] args) {
        launch();
    }

    private void updateAzimuthLabel(Label label, RangeSlider slider) {
        label.setText(String.format("Blocked Range: %.0f° to %.0f°", slider.getLowValue(), slider.getHighValue()));
    }
}