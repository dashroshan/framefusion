package com.dashroshan;

import java.util.Arrays;
import java.util.HashSet;

public class Utility {
    public static HashSet<String> allowedExtensions = new HashSet<>(
            Arrays.asList("mp4", "mov", "avi", "flv", "m4v", "webm", "3gp"));

    public static boolean isValidVideoFile(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0)
            extension = fileName.substring(i + 1);
        return allowedExtensions.contains(extension);
    }
}
