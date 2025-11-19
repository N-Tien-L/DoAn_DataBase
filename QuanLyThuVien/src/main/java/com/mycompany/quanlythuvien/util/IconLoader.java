package com.mycompany.quanlythuvien.util;

import javax.swing.*;
import java.awt.Image;
import java.net.URL;

/**
 * Utility class để load icon từ resources
 */
public class IconLoader {

    private static final String ICON_PATH = "/icons/32x32/";

    public static ImageIcon loadIcon(String iconName) {
        try {
            URL iconUrl = IconLoader.class.getResource(ICON_PATH + iconName);
            if (iconUrl != null) {
                return new ImageIcon(iconUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ImageIcon loadIconScaled(String iconName, int width, int height) {
        ImageIcon icon = loadIcon(iconName);
        if (icon != null) {
            Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        }
        return null;
    }

    // Icon constants
    public static final String ICON_OVERVIEW = "overview.png";
    public static final String ICON_READERS = "readers.png";
    public static final String ICON_READER = "reader.png";
    public static final String ICON_ADMIN = "admin.png";
    public static final String ICON_ACCOUNT = "account.png";
    public static final String ICON_LIBRARIAN = "librarian.png";
    public static final String ICON_BOOK = "book.png";
    public static final String ICON_BORROW = "borrow.png";
    public static final String ICON_COPY = "copy.png";
    public static final String ICON_MONEY = "money.png";
    public static final String ICON_PAID = "paid.png";
    public static final String ICON_UNPAID = "unpaid.png";
    public static final String ICON_OVERDUE = "overdue.png";
    public static final String ICON_VIOLATION = "violation.png";
    public static final String ICON_STATS = "stats.png";
    public static final String ICON_PHAT = "phat.png";
    public static final String ICON_ACTIVE_USER = "active_user.png";
}
