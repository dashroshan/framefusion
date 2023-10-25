package com.dashroshan;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
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

    private void setThumbnailAndInfo() throws IOException {
        FFprobe ffprobe = new FFprobe("ffprobe.exe");
        FFmpeg ffmpeg = new FFmpeg("ffmpeg.exe");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        FFmpegProbeResult in = ffprobe.probe(inputFilePath);
        FFmpegFormat format = in.getFormat();
        FFmpegStream stream = in.getStreams().get(0);

        inputFileName.setText(inputFilePath.substring(inputFilePath.lastIndexOf('\\') + 1));
        inputCodec.setText(stream.codec_name.toUpperCase() + " Codec");
        inputDuration.setText(String.format("%.2fs", format.duration));
        inputResolution.setText(String.format("%d x %dpx", stream.width, stream.height));
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

        String outputFilePath = inputFilePath.substring(0, inputFilePath.lastIndexOf('\\'));

        FFmpegBuilder builderThumb = new FFmpegBuilder()
                .setInput(in)
                .addOutput(outputFilePath + "thumb.jpg").addExtraArgs("-vframes", "1")
                .done();
        executor.createJob(builderThumb).run();

        File thumbImg = new File(outputFilePath + "thumb.jpg");
        Image fxImage = new Image(thumbImg.toURI().toString());
        videoThumbnail.setImage(fxImage);
        thumbImg.delete();
    }

    @FXML
    protected void process() throws IOException {
        FileChooser file_chooser = new FileChooser();
        file_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Video files", "*.mp4", "*.mov", "*.avi", "*.flv", "*.m4v", "*.webm", "*.3gp"));

        File file = file_chooser.showSaveDialog(videoThumbnail.getScene().getWindow());

        if (file == null || !Utility.isValidVideoFile(file.getName())) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Unsupported format");
            alert.setContentText(
                    "You were trying to save your video file in a format not supported by FrameFusion. Please select one of the following: MP4, MOV, AVI, FLV, M4V, WEBM, 3GP");
            alert.show();
            return;
        }

        String outputFilePath = file.getAbsolutePath();

        FFprobe ffprobe = new FFprobe("ffprobe.exe");
        FFmpeg ffmpeg = new FFmpeg("ffmpeg.exe");

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegProbeResult in = ffprobe.probe(inputFilePath);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(in)
                .addOutput(outputFilePath)
                .setVideoQuality(0.5)
                .done();

        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            final double inputDuration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            @Override
            public void progress(Progress progress) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (progress.status.toString().equals("end"))
                            progressBar.setProgress(0);
                        else
                            progressBar.setProgress(Math.min(progress.out_time_ns / inputDuration_ns, 0.07));
                    }
                });
            }
        });

        Thread processingThread = new Thread(job);
        processingThread.setDaemon(true);
        processingThread.start();
    }

    @FXML
    protected void dragAndDropDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();

        if (event.getGestureSource() != dragAndDrop && db.hasFiles()) {
            String fileName = db.getFiles().get(0).getName().toLowerCase();
            if (Utility.isValidVideoFile(fileName))
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        } else
            event.consume();
    }

    @FXML
    protected void dragAndDropDragDropped(DragEvent event) throws IOException {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            inputFilePath = db.getFiles().get(0).toString();
            success = true;
        }
        setThumbnailAndInfo();
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    protected void dragAndDropClicked() throws IOException {
        FileChooser file_chooser = new FileChooser();
        file_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Video files", "*.mp4", "*.mov", "*.avi", "*.flv", "*.m4v", "*.webm", "*.3gp"));

        File file = file_chooser.showOpenDialog(videoThumbnail.getScene().getWindow());

        if (file != null && Utility.isValidVideoFile(file.getName())) {
            inputFilePath = file.getAbsolutePath();
            setThumbnailAndInfo();
        }
    }
}
