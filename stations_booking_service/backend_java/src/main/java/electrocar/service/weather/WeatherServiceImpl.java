package electrocar.service.weather;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import electrocar.dto.weather.WeatherRequestDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
    private static final String FORECASTS = "forecasts";
    private static final String PARTS = "parts";
    private static final String DAY = "day";
    private static final String TEMP_AVG = "temp_avg";

    private final Gson gson;

    @Value("${yandexWeather.request.url}")
    private String weatherRequestUrl;

    @Value("${yandexWeather.request.api}")
    private String api;

    @Override
    public List<Double> getTemperature(WeatherRequestDTO weatherRequest) throws ParseException, IOException {
        String lat = weatherRequest.getLocation().getLatitude().toString();
        String lon = weatherRequest.getLocation().getLongitude().toString();

        LocalDate dateChosen = LocalDate.parse(weatherRequest.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long dayLimit = ChronoUnit.DAYS.between(LocalDate.now(), dateChosen) + 1;

        String url = weatherRequestUrl + "?lat=" + lat + "&lon=" + lon + "&limit=" + dayLimit;
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
}
