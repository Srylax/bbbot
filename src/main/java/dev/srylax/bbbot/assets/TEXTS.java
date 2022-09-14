package dev.srylax.bbbot.assets;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class TEXTS {


    public static String get(String key, String bundle, Locale locale,String... arguments) {
        return MessageFormat.format(ResourceBundle.getBundle(bundle, locale).getString(key), (Object[]) arguments);
    }

    public static String get(String key, String... arguments) {
        return get(key, "lang/Texts", Locale.GERMAN, arguments);
    }
}
