package org.apmem.tools.util;

/**
 * Created by kaustubh on 27/10/17.
 */

public final class Utils {

    /**
     * Referenced from <a href="https://www.regextester.com/19" />
     */
    public static final String EMAIL_ADDRESS = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+" +
            "@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,64}[a-zA-Z0-9])?" +
            "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,25}[a-zA-Z0-9])?)+$";

    public static boolean isValidEmailAddress(String email) {
        return email.matches(EMAIL_ADDRESS);
    }

}
