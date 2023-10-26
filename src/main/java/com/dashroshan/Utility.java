package com.dashroshan;

import java.util.Arrays;
import java.util.HashSet;

public class Utility {
    public static String[] extensions = new String[] { "MP4", "MOV", "AVI", "FLV", "M4V", "WEBM", "3GP" };
    public static String[] extensionsPattern;

    public static HashSet<String> allowedExtensions = new HashSet<>(Arrays.asList(extensions));

    static {
        extensionsPattern = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++)
            extensionsPattern[i] = "*." + extensions[i].toLowerCase();
    }

    public static boolean isValidVideoFile(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0)
            extension = fileName.substring(i + 1);
        return allowedExtensions.contains(extension.toUpperCase());
    }
}
