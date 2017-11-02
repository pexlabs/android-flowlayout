/**
 * ChipListener.java
 *
 * Created by kaustubh on 26/10/17.
 *
 * Listener to track the life of chips.
 */

package org.apmem.tools.listeners;

import org.apmem.tools.model.ChipInterface;

public interface ChipListener {
    void onChipRemoved(ChipInterface chip);

    void onChipAdded(ChipInterface chip);
}