module com.example.financeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires nanohttpd;
    requires jdk.httpserver;
    requires java.net.http;
    requires java.sql;
    requires com.github.librepdf.openpdf;
    requires org.apache.poi.ooxml;
    requires org.xerial.sqlitejdbc;
    requires java.prefs;
    requires org.json;

    opens com.example.financeapp to javafx.fxml;
    opens com.example.financeapp.controllers to javafx.fxml;
    opens com.example.financeapp.models to javafx.fxml;

    exports com.example.financeapp;
    exports com.example.financeapp.controllers;
}