package Core;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger
{
    private final TextArea ui;

    public Logger(TextArea ui)
    {
        this.ui = ui;
    }

    public void log(String text)
    {
        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss:SSS");
        Date now = new Date();
        String message = String.format("[%s] %s\n", dt.format(now), text);
        Platform.runLater(() -> ui.appendText(message));
    }
}
