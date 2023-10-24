package com.dashroshan;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
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

public class BasicController {
    @FXML
    private TextField input;

    @FXML
    private TextField output;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ImageView thumbnail;

    private static HashSet<String> allowedExtensions = new HashSet<>(Arrays.asList("mp4", "mov", "avi"));

    private void setThumbnail() throws IOException {
        FFprobe ffprobe = new FFprobe("ffprobe.exe");
        FFmpeg ffmpeg = new FFmpeg("ffmpeg.exe");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        FFmpegProbeResult in = ffprobe.probe(input.getText());

        FFmpegBuilder builderThumb = new FFmpegBuilder()
                .setInput(in)
                .addOutput(output.getText() + "thumb.jpg").addExtraArgs("-vframes", "1")
                .done();
        executor.createJob(builderThumb).run();

        File thumbImg = new File(output.getText() + "thumb.jpg");
        Image fxImage = new Image(thumbImg.toURI().toString());
        thumbnail.setImage(fxImage);
        thumbImg.delete();
    }

    private void showDetails() throws IOException {
        FFprobe ffprobe = new FFprobe("ffprobe.exe");

        FFmpegProbeResult probeResult = ffprobe
                .probe(input.getText());

        FFmpegFormat format = probeResult.getFormat();
        System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
                format.filename,
                format.format_long_name,
                format.duration);

        FFmpegStream stream = probeResult.getStreams().get(0);
        System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",
                stream.codec_long_name,
                stream.width,
                stream.height);
    }

    @FXML
    protected void process() throws IOException {
        setThumbnail();
        showDetails();

        FFprobe ffprobe = new FFprobe("ffprobe.exe");
        FFmpeg ffmpeg = new FFmpeg("ffmpeg.exe");

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegProbeResult in = ffprobe.probe(input.getText());

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(in)
                .addOutput(output.getText() + "output.mp4")
                .setVideoQuality(0.5)
                .done();

        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            @Override
            public void progress(Progress progress) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (progress.status.toString().equals("end"))
                            progressBar.setProgress(0);
                        else
                            progressBar.setProgress(progress.out_time_ns / duration_ns);
                    }
                });
            }
        });

        Thread processingThread = new Thread(job);
        processingThread.setDaemon(true);
        processingThread.start();
    }

    private boolean isValidVideoFile(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0)
            extension = fileName.substring(i + 1);
        return allowedExtensions.contains(extension);
    }

    @FXML
    protected void dragover(DragEvent event) {
        Dragboard db = event.getDragboard();

        if (event.getGestureSource() != thumbnail && db.hasFiles()) {
            String fileName = db.getFiles().get(0).getName().toLowerCase();
            if (isValidVideoFile(fileName))
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        } else
            event.consume();
    }

    @FXML
    protected void dragdropped(DragEvent event) throws IOException {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            input.setText(db.getFiles().get(0).toString());
            success = true;
        }
        setThumbnail();
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    protected void browse() throws IOException {
        FileChooser file_chooser = new FileChooser();
        file_chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Video files", "*.mp4", "*.mov", "*.avi"));

        File file = file_chooser.showOpenDialog(thumbnail.getScene().getWindow());

        if (file != null && isValidVideoFile(file.getName())) {
            input.setText(file.getAbsolutePath());
            setThumbnail();
        }
    }
}
