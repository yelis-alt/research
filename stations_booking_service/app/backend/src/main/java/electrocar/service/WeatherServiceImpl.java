package electrocar.service;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import electrocar.dto.LocationDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WeatherServiceImpl implements WeatherService {
    private final static String BASE_URL = "https://api.weather.yandex.ru/v2/forecast/";
    private final static Integer DAY_LIMIT = 10;
    private final static String API = "f285fb77-4676-4fa3-902b-8374b8f9cd4e";

    @Override
    public Integer getTemperature(LocationDTO locationDTO,
                                  String date) throws IOException {
        String lat = locationDTO.getLatitude();
        String lon = locationDTO.getLongitude();
        String url = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + DAY_LIMIT;

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("X-Yandex-API-Key", API);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            HttpEntity httpEntity = response.getEntity();
            String responseString = EntityUtils.toString(httpEntity, "UTF-8");

            JSONObject jsonResponse = new JSONObject(responseString);
            JSONArray forecasts = jsonResponse
                    .getJSONArray("forecasts");
        }

       return null;
    }
}
