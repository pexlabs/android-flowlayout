package org.apmem.tools.util;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import org.apmem.tools.model.Chip;
import org.apmem.tools.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Combines all the emails like to/cc/bcc into one with respective prefixes & comma
     * separates values
     * e.g. if toList -> abc@gmail.com & pqr@gmail.com and ccList -> xyz@yahoo.com
     * then resultList -> abc@gmail.com, pqr@gmail.com, cc xyz@yahoo.com
     * @param toList
     * @param ccList
     * @param bccList
     * @return
     */
    @NonNull public static List<ChipInterface> getAllEmails(@NonNull List<ChipInterface> toList,
            @NonNull List<ChipInterface> ccList, @NonNull List<ChipInterface> bccList) {

        List<ChipInterface> allEmails = new ArrayList<>();
        String comma = "";

        // Populate for toList
        for (int i = 0; i < toList.size(); i++) {
            if (i > 0) {
                comma = ", ";
            }
            allEmails.add(new Chip(comma + toList.get(i).getLabel(), toList.get(i).getInfo(),
                    toList.get(i).isAutoCompleted()));
        }

        String prefix;
        if (allEmails.size() > 0) {
            comma = ", ";
        }

        // Populate for ccList, only first email address will have prefix as cc and all email
        // addresses will have comma in between them
        if (!ccList.isEmpty()) {
            for (int i = 0; i < ccList.size(); i++) {
                if (i > 0) {
                    comma = ", ";
                    prefix = "";
                } else {
                    prefix = "cc ";
                }
                allEmails.add(new Chip(comma + prefix +ccList.get(i).getLabel(),
                        ccList.get(i).getInfo(), ccList.get(i).isAutoCompleted()));
            }
        }

        if (allEmails.size() > 0) {
            comma = ", ";
        }

        // Populate for bccList, only first email address will have prefix as bcc and all email
        // addresses will have comma in between them
        if (!bccList.isEmpty()) {
            for (int i = 0; i < bccList.size(); i++) {
                if (i > 0) {
                    comma = ", ";
                    prefix = "";
                } else {
                    prefix = "bcc ";
                }
                allEmails.add(new Chip(comma + prefix + bccList.get(i).getLabel(),
                        bccList.get(i).getInfo(), bccList.get(i).isAutoCompleted()));
            }
        }

        return allEmails;
    }

    /**
     * Gets the a string from all the emails with the count of extra email addresses if any
     * @param allEmails
     * @param layoutWidth
     * @param textSize
     * @param typeface
     * @return
     */
    @NonNull public static String getSingleLineEmailString(@NonNull List<ChipInterface> allEmails,
            int layoutWidth, int textSize, Typeface typeface) {

        // The paint object to calculate bounds of TextView, to check if it fits in a single line
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTypeface(typeface);
        paint.setTextSize(textSize);

        StringBuilder builder = new StringBuilder();
        int j = 1;
        // No matter what, we have to always add 1st address
        builder.append(allEmails.get(0).getLabel());
        int totalCount = allEmails.size() - 1;

        while (bounds.width() < layoutWidth) {
            String textTillNow = builder.toString();
            // We have to consider the count as well before checking for the validity of the length
            textTillNow = textTillNow + " + " + String.valueOf(totalCount);

            // If we have more more emails
            if (j < allEmails.size()) {
                // Check if next email fits into single line, by appending it to the previous list
                String nextLabel = allEmails.get(j).getLabel();
                String nextText = textTillNow + nextLabel;
                // Calculate bounds of the textView
                paint.getTextBounds(nextText, 0, nextText.length(), bounds);
                if (bounds.width() < layoutWidth) {
                    builder.append(nextLabel);
                    j++;
                    totalCount--;
                }
            } else {
                break;
            }
        }
        // Don't append count if it is not greater than 0
        if (totalCount > 0) {
            builder.append(" + ");
            builder.append(String.valueOf(totalCount));
        }
        return builder.toString();
    }
}
