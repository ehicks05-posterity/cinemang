package net.ehicks.cinemang;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

public class DailyFile {
    private static final Logger log = LoggerFactory.getLogger(DailyFile.class);

    public static Path getDailyIdFile() {
        Path dailyIdFileUnzipped = getDailyFileUnzippedPath();
        if (dailyIdFileUnzipped.toFile().exists())
            return dailyIdFileUnzipped;

        Path dailyIdFile = downloadDailyIdFile();
        if (dailyIdFile == null)
            return null;

        return toTextFile(dailyIdFile);
    }

    private static String getDailyFilename() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Z"));
        if (dateTime.getHour() < 9)
            dateTime = dateTime.minusDays(1);

        String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MM_dd_YYYY"));

        return "movie_ids_" + formattedDate + ".json.gz";
    }

    private static Path getDailyFilePath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir, getDailyFilename());
    }

    private static Path getDailyFileUnzippedPath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir, getDailyFilename().replace(".json.gz", ".txt"));
    }

    private static Path downloadDailyIdFile() {
        try (InputStream in = new URL("http://files.tmdb.org/p/exports/" + getDailyFilename()).openStream();) {
            Path temp = Files.createFile(getDailyFilePath());
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            return temp;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private static Path toTextFile(Path dailyIdFile) {
        Path unzipped = Paths.get(dailyIdFile.toString().replace(".json.gz", ".txt"));
        try (InputStream fileStream = new FileInputStream(dailyIdFile.toFile());
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, Charset.defaultCharset());
                BufferedReader buffered = new BufferedReader(decoder);
                FileWriter fileWriter = new FileWriter(unzipped.toFile());
                PrintWriter printWriter = new PrintWriter(fileWriter)) {
            ObjectMapper mapper = new ObjectMapper();

            String line;
            while ((line = buffered.readLine()) != null) {
                int id = mapper.readTree(line).get("id").asInt();
                printWriter.println(id);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return null;
        }

        return unzipped;
    }
}
