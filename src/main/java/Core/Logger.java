package Core;

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
        ui.appendText(String.format("[%s] %s\n", dt.format(now), text));
    }
}
