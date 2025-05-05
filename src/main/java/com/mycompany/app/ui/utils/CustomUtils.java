package com.mycompany.app.ui.utils;

import java.io.File;

public class CustomUtils {

    final static File dir = new File(System.getProperty("user.dir") + "/src/main/resources/cardImages");

    public static String generateRandomCard() {
        File[] files = dir.listFiles();
        assert files != null;
        int idx = (int) (Math.random() * files.length);
        return "cardImages/" + files[idx].getName();
    }
}
