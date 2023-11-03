# FrameFusion

FrameFusion is a video utility tool to resize, compress, trim, and convert videos in mp4, mov, avi, flv, m4v, and webm formats.

### [Download pre-built FrameFusion EXE](https://drive.google.com/file/d/10Y14B4L-YtE6gxA3_0gejahMXvZ-QbsS/view?usp=sharing)

## Screenshots

![](/screenshot_1.png)

![](/screenshot_2.png)

## Development guide

### Setup

- Install Maven from here: https://maven.apache.org/download.cgi
- Add Maven to windows PATH.
- Install Maven for Java extension for VSCode.
- Clone this repo.
- Download ffmpeg: https://drive.google.com/file/d/1BgL22p1T_4ktdaAa9Qo4VMb2JIwpKoMq and put the 2 EXEs in `src/main/resources/com/dashroshan`

### Run

- In VSCode expand MAVEN -> framefusion -> plugins -> javafx.
- Click compile, then run.

### Build EXE

- Download Launch4j from here: https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50
- In cmd, open the project directory and type `mvn package`
- Open Launch4j, select the `framefusion-x.y.z.jar` from target folder of the project for JAR, select the `icon.ico` file from `src/main/resources/com/dashroshan` for the icon, and pick output EXE location.
- Click the gear icon at top to build the EXE.