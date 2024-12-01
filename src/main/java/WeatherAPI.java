import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class WeatherAPI {

    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String API_KEY = "69eafc51-d426-4c4a-bcdb-776afcf358eb";

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);

        double lat = 0;
        double lon = 0;
        int limit = 0;

        while (true) {
            System.out.print("Введите широту в диапазоне от -90.0 до 90.0(Пример: 55.75): ");
            if (scanner.hasNextDouble()) {
                lat = scanner.nextDouble();
                if (lat >= -90.0 && lat <= 90.0) break;
            }
            System.out.println("Error: широта должна быть числом в диапазоне от -90.0 до 90.0.");
            scanner.nextLine();
        }

        while (true) {
            System.out.print("Введите долготу в диапазоне от -180.0 до 180.0(Пример: 37.62): ");
            if (scanner.hasNextDouble()) {
                lon = scanner.nextDouble();
                if (lon >= -180.0 && lon <= 180.0) break;
            }
            System.out.println("Error: долгота должна быть числом в диапазоне от -180.0 до 180.0.");
            scanner.nextLine();
        }

        while (true) {
            System.out.print("Введите количество дней для прогноза от 1 до 11: ");
            if (scanner.hasNextInt()) {
                limit = scanner.nextInt();
                if (limit >= 1 && limit <= 11) break;
            }
            System.out.println("Error: количество дней должно быть целым числом от 1 до 11.");
            scanner.nextLine();
        }

        String jsonResponse = getWeather(lat, lon, limit);

        System.out.println("Полный JSON-ответ: " + jsonResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        int currentTemp = rootNode.path("fact").path("temp").asInt();
        System.out.println("Температура сейчас: " + currentTemp + "°C");

        String timezoneName = rootNode.path("info").path("tzinfo").path("name").asText();
        System.out.println("Населенный пункт: " + timezoneName);

        JsonNode forecasts = rootNode.path("forecasts");
        double averageTemp = calcAvgTemp(forecasts, limit);
        System.out.println("Средняя температура за " + limit + " дней: " +  Math.round(averageTemp) + "°C");
    }

    private static String getWeather(double lat, double lon, int limit) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String uri = String.format(Locale.US, "%s?lat=%.2f&lon=%.2f&limit=%d", API_URL, lat, lon, limit);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("X-Yandex-Weather-Key", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ошибка запроса: " + response.statusCode());
        }

        return response.body();
    }

    private static double calcAvgTemp(JsonNode forecasts, int limit) {
        int sum = 0;
        int count = Math.min(forecasts.size(), limit);

        for (int i = 0; i < count; i++) {
            JsonNode dayTemp = forecasts.get(i).path("parts").path("day").path("temp_avg");
            sum += dayTemp.asInt();
        }
        return count > 0 ? (double) sum / count : 0;
    }
}