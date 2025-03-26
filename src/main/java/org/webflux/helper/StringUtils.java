package org.webflux.helper;

import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class StringUtils {

    public static String camelToSnake(String camelCaseStr) {
        if (Objects.isNull(camelCaseStr) || camelCaseStr.isEmpty()) {
            return camelCaseStr;
        }

        String snakeCaseStr = camelCaseStr
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase();

        return snakeCaseStr;
    }

    public static String camelToSnakeWithLetterCheck(String camelCaseStr) {
        if (Strings.isEmpty(camelCaseStr)) {
            return camelCaseStr;
        }

        StringBuilder snakeCaseStr = new StringBuilder();

        for (char ch : camelCaseStr.toCharArray()) {
            // Check if the character is uppercase
            if (Character.isUpperCase(ch)) {
                // Append an underscore before the uppercase letter (except for the first character)
                if (snakeCaseStr.length() > 0) {
                    snakeCaseStr.append('_');
                }
                // Convert the uppercase letter to lowercase
                snakeCaseStr.append(Character.toLowerCase(ch));
            } else {
                snakeCaseStr.append(ch);
            }
        }

        return snakeCaseStr.toString();
    }

    public static String merge(String ...args) {
        return String.join(", ", Arrays.stream(args).filter(x -> Strings.isNotBlank(x)).collect(Collectors.toList()));
    }
}