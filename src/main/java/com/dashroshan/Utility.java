package com.dashroshan;

import java.util.Arrays;
import java.util.HashSet;

public class Utility {
    // Supported video formats
    public static String[] extensions = new String[] { "MP4", "MOV", "AVI", "FLV", "M4V", "WEBM" };
    public static String commaSeparatedExtensions = "";

    public static String[] extensionsPattern;
    public static HashSet<String> allowedExtensions = new HashSet<>(Arrays.asList(extensions));

    // Convert all extensions from "XYZ" to "*.xyz" for use with JavaFX filechooser
    static {
        extensionsPattern = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            extensionsPattern[i] = "*." + extensions[i].toLowerCase();
            commaSeparatedExtensions += extensions[i];
            if (i == extensions.length - 2)
                commaSeparatedExtensions += ", and ";
            else if (i != extensions.length - 1)
                commaSeparatedExtensions += ", ";
        }
    }

    // Checks if a file is in a supported video format
    public static boolean isValidVideoFile(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0)
            extension = fileName.substring(i + 1);
        return allowedExtensions.contains(extension.toUpperCase());
    }
}
