module com.example.astromanager {
    requires javafx.fxml;
    requires org.controlsfx.controls;


    opens me.jaime.astromanager to javafx.fxml;
    opens me.jaime.astromanager.objects to javafx.base;
    opens me.jaime.astromanager.enums to javafx.base;
    exports me.jaime.astromanager;
}