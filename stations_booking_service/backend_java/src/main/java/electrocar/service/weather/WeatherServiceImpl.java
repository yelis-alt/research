package electrocar.service.weather;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import electrocar.dto.weather.WeatherRequestDTO;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast/";
    private static final String API_JSON = "api.json";
    private static final String FORECASTS = "forecasts";
    private static final String PARTS = "parts";
    private static final String DAY = "day";
    private static final String TEMP_AVG = "temp_avg";
    private static final Gson gson = new Gson();

    private String api;

    @Override
    public List<Double> getTemperature(WeatherRequestDTO weatherRequest) throws ParseException, IOException {
        String lat = weatherRequest.getLocation().getLatitude().toString();
        String lon = weatherRequest.getLocation().getLongitude().toString();

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateChosen = LocalDate.parse(weatherRequest.getDate(), pattern);
        long dayLimit = ChronoUnit.DAYS.between(LocalDate.now(), dateChosen) + 1;

        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + dayLimit;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("X-Yandex-API-Key", api);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            HttpEntity httpEntity = response.getEntity();
            String responseString = EntityUtils.toString(httpEntity, "UTF-8");
            JsonObject responseJson = gson.fromJson(responseString, JsonObject.class);
            Double tempAvg = responseJson
                    .getAsJsonArray(FORECASTS)
                    .get((int) dayLimit - 1)
                    .getAsJsonObject()
                    .get(PARTS)
                    .getAsJsonObject()
                    .get(DAY)
                    .getAsJsonObject()
                    .get(TEMP_AVG)
                    .getAsDouble();

            return List.of(tempAvg);
        } else {

            throw new InternalError("Unable to determine temperature for this location on "
                    + weatherRequest.getDate().replace("-", "."));
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initApi() throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(API_JSON));
        JsonObject jsonApi = gson.fromJson(bufferedReader, JsonObject.class);
        api = String.valueOf(jsonApi.get("yandex_weather")).replace("\"", "");
    }
}
