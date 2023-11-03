package com.dashroshan;

import java.util.HashMap;

public class Info {
    public static HashMap<String, String> messages = new HashMap<>();

    static {
        messages.put("Resolution",
                "Use this to set the output video resolution as Width x Height in pixels. To maintain a constant ratio and just change one parameter, set the desired Ratio option.");
        messages.put("Quality",
                "Use the slider to set the output video quality to reduce the file size. Decreasing it too much might bring very noticable blurring and blocky artefacts to your video.");
        messages.put("Duration",
                "Use this to trim your video starting and ending time. The first set of inputs are Hours Minutes Seconds for the start and the second set of inputs are for the end time.");
        messages.put("Format", "Use this to select the output format for your video amongst the available options: "
                + Utility.commaSeparatedExtensions + ".");
        messages.put("Ratio",
                "Use this to constraint the output resolution. Variable allows you to change both width and height as needed. Fixed height or width, allows you to keep one parameter fixed and change the other maintaing the original video resolution ratio.");
        messages.put("Finished",
                "Conversion completed successfully with your given output settings. You can convert another video file by clicking or dragging it over to the Drag & Drop area.");
    }
}
