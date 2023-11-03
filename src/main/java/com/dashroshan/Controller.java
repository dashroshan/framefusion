package com.dashroshan;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

public class Controller {
    private String inputFilePath = "";
    private double originalRatio;
    private int originalHeight, originalWidth;

    @FXML
    private ComboBox<String> outputFormat;

    @FXML
    private ComboBox<String> resolutionRatio;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ImageView videoThumbnail;

    @FXML
    private ImageView dragAndDrop;

    @FXML
    private Label inputCodec;

    @FXML
    private Label inputDuration;

    @FXML
    private Label inputResolution;

    @FXML
    private Label inputFileName;

    @FXML
    private Label outputQualityPercentage;

    @FXML
    private Slider outputQuality;

    @FXML
    private TextField outputResolutionWidth;

    @FXML
    private TextField outputResolutionHeight;

    @FXML
    private TextField outputDurationStartH;

    @FXML
    private TextField outputDurationStartM;

    @FXML
    private TextField outputDurationStartS;

    @FXML
    private TextField outputDurationEndH;

    @FXML
    private TextField outputDurationEndM;

    @FXML
    private TextField outputDurationEndS;

    @FXML
    private ImageView saveButton;

    // Enable or disable the output setting controls
    private void switchControls(boolean disabled) {
        outputResolutionHeight.setDisable(disabled);
        outputResolutionWidth.setDisable(disabled);
        outputQuality.setDisable(disabled);
        outputDurationStartH.setDisable(disabled);
        outputDurationStartM.setDisable(disabled);
        outputDurationStartS.setDisable(disabled);
        outputDurationEndH.setDisable(disabled);
        outputDurationEndM.setDisable(disabled);
        outputDurationEndS.setDisable(disabled);
        outputFormat.setDisable(disabled);
        resolutionRatio.setDisable(disabled);
        saveButton.setDisable(disabled);
        saveButton.setOpacity(disabled ? 0.5 : 1);
        saveButton.setCursor(disabled ? Cursor.DEFAULT : Cursor.HAND);
    }

    // Initialize the video thumbnail, info, and default output settings
    private void setThumbnailAndInfo() throws IOException {
        // Load FFMPEG
        FFprobe ffprobe = new FFprobe("ffprobe.exe");
        FFmpeg ffmpeg = new FFmpeg("ffmpeg.exe");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // Get info about the input video
        FFmpegProbeResult in = ffprobe.probe(inputFilePath);
        FFmpegFormat format = in.getFormat();
        FFmpegStream stream = in.getStreams().get(0);

        // Set input video info in the preview area
        inputFileName.setText(inputFilePath.substring(inputFilePath.lastIndexOf('\\') + 1));
        inputCodec.setText(stream.codec_name.toUpperCase() + " Codec");
        inputDuration.setText(String.format("%.2fs", format.duration));
        inputResolution.setText(String.format("%d x %dpx", stream.width, stream.height));

        // Set default output settings same as that of input
        originalRatio = (double) stream.width / (double) stream.height;
        originalHeight = stream.height;
        originalWidth = stream.width;
        outputResolutionWidth.setText(Integer.toString(stream.width));
        outputResolutionHeight.setText(Integer.toString(stream.height));
        outputDurationStartH.setText("0");
        outputDurationStartM.setText("0");
        outputDurationStartS.setText("0");
        int sec = (int) format.duration;
        int min = sec / 60;
        int hr = min / 60;
        min %= 60;
        sec %= 60;
        outputDurationEndH.setText(Integer.toString(hr));
        outputDurationEndM.setText(Integer.toString(min));
        outputDurationEndS.setText(Integer.toString(sec));
        outputFormat.getSelectionModel()
                .select(inputFilePath.substring(inputFilePath.lastIndexOf('.') + 1).toUpperCase());
        resolutionRatio.getSelectionModel().select("Variable");

        // Ready FFMPEG to extract the thumbnail
        String outputFilePath = inputFilePath.substring(0, inputFilePath.lastIndexOf('\\'));
        FFmpegBuilder builderThumb = new FFmpegBuilder()
                .setInput(in)
                .addOutput(outputFilePath + "thumb.jpg").addExtraArgs("-vframes", "1")
                .done();

        // To avoid blocking the JavaFX UI thread, run the thumbnail extraction using
        // FFMPEG in another thread
        FFmpegJob job = executor.createJob(builderThumb, new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                // When thumbnail extracted update UI in runLater as JavaFX UI thread might not
                // be free at this instant
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (progress.status.toString().equals("end")) {
                            // The thumbnail is saved in same location as input video
                            // Load it to the imageview and then delete
                            File thumbImg = new File(outputFilePath + "thumb.jpg");
                            Image fxImage = new Image(thumbImg.toURI().toString());
                            videoThumbnail.setImage(fxImage);
                            thumbImg.delete();

                            // Enable the output setting controls
                            switchControls(false);
                        }
                    }
                });
            }
        });

        // Set thread as daemon to avoid conversion continuing if app is closed
        // Then start the thread
        Thread processingThread = new Thread(job);
        processingThread.setDaemon(true);
        processingThread.start();
    }

    // Called when user clicks the Save button
    @FXML
    protected void convertAndSave() throws IOException {
        // Create a file chooser window to select output file name and location
        FileChooser file_chooser = new FileChooser();

        // Set the video format from Output format selection box in the file chooser
        String format = outputFormat.getValue().toLowerCase();
        file_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Video file", "*." + format));
        File file = file_chooser.showSaveDialog(videoThumbnail.getScene().getWindow());

        // If user cancels the file chooser window stop the process here
        if (file == null)
            return;

        // Disable all output setting controls
        switchControls(true);

        // Calculate the height and width based on the ratio dropdown box
        String ratio = resolutionRatio.getValue();
        int opHeight, opWidth;
        if (ratio == "Fixed height") {
            opWidth = Integer.parseInt(outputResolutionWidth.getText());
            opHeight = (int) (opWidth / originalRatio);
        } else if (ratio == "Fixed width") {
            opHeight = Integer.parseInt(outputResolutionHeight.getText());
            opWidth = (int) (opHeight * originalRatio);
        } else {
            opWidth = Integer.parseInt(outputResolutionWidth.getText());
            opHeight = Integer.parseInt(outputResolutionHeight.getText());
        }

        // Most video codecs dont allow odd height or width in pixels
        // So make both even if not already
        if (opWidth % 2 == 1)
            opWidth++;
        if (opHeight % 2 == 1)
            opHeight++;

        // Get output video settings
        double opQuality = outputQuality.getValue() / 100.0;
        long startH = Long.parseLong(outputDurationStartH.getText());
        long startM = Long.parseLong(outputDurationStartM.getText());
        long startS = Long.parseLong(outputDurationStartS.getText());
        long start = startH * 3600 + startM * 60 + startS;
        long endH = Long.parseLong(outputDurationEndH.getText());
        long endM = Long.parseLong(outputDurationEndM.getText());
        long endS = Long.parseLong(outputDurationEndS.getText());
        long end = endH * 3600 + endM * 60 + endS;

        // Get output file path
        String outputFilePath = file.getAbsolutePath();

        // Load FFMPEG
        FFprobe ffprobe = new FFprobe("ffprobe.exe");
        FFmpeg ffmpeg = new FFmpeg("ffmpeg.exe");

        // Load the video into FFMPEG along with the output video settings
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegProbeResult in = ffprobe.probe(inputFilePath);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(in)
                .addOutput(outputFilePath)
                .setFormat(format)
                .setVideoQuality(opQuality)
                .setVideoHeight(opHeight)
                .setVideoWidth(opWidth)
                .setStartOffset(start, TimeUnit.SECONDS)
                .setDuration(end - start, TimeUnit.SECONDS)
                .done();

        // To avoid blocking the JavaFX UI thread, run the conversion in another thread
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            final double inputDuration_ns = (end - start) * TimeUnit.SECONDS.toNanos(1);

            // Gets called for every 1% change in conversion progress
            @Override
            public void progress(Progress progress) {
                // Update progressbar in runLater as UI thread might not be free this instant
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (progress.status.toString().equals("end")) {
                            // At end, reset progressbar to 0 and enable the output setting controls again
                            progressBar.setProgress(0);
                            switchControls(false);
                            showHelp("Finished");
                        } else
                            progressBar.setProgress(progress.out_time_ns / inputDuration_ns);
                    }
                });
            }
        });

        // Set thread as daemon to avoid conversion continuing if app is closed
        // Then start the thread
        Thread processingThread = new Thread(job);
        processingThread.setDaemon(true);
        processingThread.start();
    }

    // Called when users drags some file over the drag and drop area but hadnot
    // dropped it yet
    @FXML
    protected void dragAndDropDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();

        if (event.getGestureSource() != dragAndDrop && db.hasFiles()) {
            // If dragged file is in a supported video format allow the user to drop it
            String fileName = db.getFiles().get(0).getName().toLowerCase();
            if (Utility.isValidVideoFile(fileName))
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        } else
            event.consume();
    }

    // Called when user drops a file over the drag and drop area
    @FXML
    protected void dragAndDropDragDropped(DragEvent event) throws IOException {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            inputFilePath = db.getFiles().get(0).toString();
            success = true;

            // Show input video thumbnail and info
            setThumbnailAndInfo();
        }
        event.setDropCompleted(success);
        event.consume();
    }

    // Handles click event for the drag and drop area to select the input file
    @FXML
    protected void dragAndDropClicked() throws IOException {
        // Opens a filechooser with available video extension options
        FileChooser file_chooser = new FileChooser();
        file_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Video file", Utility.extensionsPattern));
        File file = file_chooser.showOpenDialog(videoThumbnail.getScene().getWindow());

        // Checks if user clicked cancel or selected an unsupported file type
        if (file != null && Utility.isValidVideoFile(file.getName())) {
            inputFilePath = file.getAbsolutePath();

            // Show input video thumbnail and info
            setThumbnailAndInfo();
        }
    }

    // Controls output resolution width and height based on the selected ratio type
    @FXML
    protected void ratioChanged(ActionEvent action) {
        String ratio = resolutionRatio.getValue();
        if (ratio == "Fixed height") {
            outputResolutionHeight.setText("");
            outputResolutionWidth.setText(Integer.toString(originalWidth));
            outputResolutionHeight.setDisable(true);
            outputResolutionWidth.setDisable(false);
        } else if (ratio == "Fixed width") {
            outputResolutionWidth.setText("");
            outputResolutionHeight.setText(Integer.toString(originalHeight));
            outputResolutionHeight.setDisable(false);
            outputResolutionWidth.setDisable(true);
        } else {
            outputResolutionWidth.setText(Integer.toString(originalWidth));
            outputResolutionHeight.setText(Integer.toString(originalHeight));
            outputResolutionHeight.setDisable(false);
            outputResolutionWidth.setDisable(false);
        }
    }

    // Shows help info alert
    // Help info texts for given title is stored in the Info class
    private void showHelp(String title) {
        Alert alert = new Alert(AlertType.NONE, Info.messages.get(title), ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        // Set favicon for the alert window
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        Image favicon = new Image(App.class.getResource("icon.png").toExternalForm());
        stage.getIcons().add(favicon);

        // Show alert
        alert.setTitle(title);
        alert.show();
    }

    // Help info alerts for all output settings available
    @FXML
    protected void helpResolution() {
        showHelp("Resolution");
    }

    @FXML
    protected void helpQuality() {
        showHelp("Quality");
    }

    @FXML
    protected void helpDuration() {
        showHelp("Duration");
    }

    @FXML
    protected void helpFormat() {
        showHelp("Format");
    }

    @FXML
    protected void helpRatio() {
        showHelp("Ratio");
    }

    // Runs once at start
    public void initialize() {
        // Disable all output setting controls
        switchControls(true);

        // Link quality percentage label text with the slider
        outputQuality.valueProperty().addListener((observable, oldValue, newValue) -> {
            outputQualityPercentage.setText(Integer.toString(newValue.intValue()) + "%");
        });

        // Add supported video formats in the Output format selection box
        outputFormat.getItems().addAll(Utility.extensions);
        resolutionRatio.getItems().addAll("Fixed height", "Fixed width", "Variable");
    }
}
