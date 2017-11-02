package org.apmem.tools.util;

import android.util.Patterns;

/**
 * Created by kaustubh on 27/10/17.
 */

public final class Utils {

    public static boolean isValidEmailAddress(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
