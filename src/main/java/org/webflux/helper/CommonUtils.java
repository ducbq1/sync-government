package org.webflux.helper;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;

@Log4j2
public class CommonUtils {
    private static final ResourceBundle RESOURCE_BUNDLE = getResource();
    private static ResourceBundle getResource() {
        try {
            return ResourceBundle.getBundle(Constants.FILE_MESS, new UTF8Control());
        } catch (Exception ex) {
            log.error("ERROR! getResourceBundle: " + ex.getMessage());
        }
        return null;
    }

    public static String getValueFileMess(String key) {
        String value = RESOURCE_BUNDLE.containsKey(key) ? RESOURCE_BUNDLE.getString(key) : Strings.EMPTY;
        if (value.trim().length() == 0) {
            log.error("Not value with key:{}, in file properties", key);
        }
        return value;
    }

    public static Long parseStringToLong(String str) {
        if (Strings.isBlank(str)) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Date convertStringToDateWithFormat(String dateStr, String format) {
        Date date;
        if (Strings.isEmpty(dateStr)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            date = sdf.parse(dateStr);
            return date;
        } catch (ParseException ex) {
            return null;
        }
    }

    public static java.sql.Date convertStringToSQLDateFormat(String dateStr, String format) {
        Date date;
        if (Strings.isEmpty(dateStr)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            date = sdf.parse(dateStr);
            return new java.sql.Date(date.getTime());
        } catch (ParseException ex) {
            return null;
        }
    }
}
