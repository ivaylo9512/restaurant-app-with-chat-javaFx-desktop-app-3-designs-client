module  main{
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.annotation;
    requires org.apache.commons.collections4;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.json;
    requires java.prefs;

    opens controllers.base;
    opens controllers.firststyle;
    opens controllers.secondstyle;
    opens controllers.thirdstyle;
    opens helpers;
    opens application;
}