package net.ehicks.cinemang;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Changes {
    public static List<Integer> getChanges(String apiKey) {
        List<String> dateRange = getDateRange();

        WebClient client = WebClient.create("https://api.themoviedb.org");
        ChangeResponse changeResponse = client.get()
                .uri("/3/movie/changes?api_key=" + apiKey + "&start_date=" + dateRange.get(0) + "&end_date=" + dateRange.get(1))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ChangeResponse>() {})
                .block();

        return changeResponse.results.stream().map(change -> change.id).collect(Collectors.toList());
    }

    private static List<String> getDateRange() {
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now(ZoneId.of("Etc/UTC"));
        LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
        LocalDateTime yesterdayMidnight = todayMidnight.minusDays(1);

        String start = yesterdayMidnight.format(DateTimeFormatter.ISO_DATE_TIME);
        String end = todayMidnight.format(DateTimeFormatter.ISO_DATE_TIME);

        return Arrays.asList(start, end);
    }

    public static class ChangeResponseItem {
        int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class ChangeResponse {
        List<ChangeResponseItem> results;

        public List<ChangeResponseItem> getResults() {
            return results;
        }

        public void setResults(List<ChangeResponseItem> results) {
            this.results = results;
        }
    }
}
