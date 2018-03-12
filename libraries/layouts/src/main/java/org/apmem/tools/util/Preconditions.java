package org.apmem.tools.util;

public class Preconditions {

    public static void checkIfCollapseSupported(boolean collapsible) {
        if (!collapsible) {
            throw new IllegalStateException("expand operation not supported for non collapsible view");
        }
    }
}
