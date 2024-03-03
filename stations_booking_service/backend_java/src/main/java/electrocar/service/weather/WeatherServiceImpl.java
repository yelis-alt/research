package electrocar.service.weather;

import electrocar.dto.weather.WeatherRequestDTO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
    private static final String HOURLY = "hourly";
    private static final String TEMP = "temperature_2m";
    private static final int HOURS_IN_DAY = 24;

    private final RestTemplate restTemplate;

    @Value("${openMeteo.request.url}")
    private String weatherRequestUrl;

    @Override
    @SuppressWarnings("unchecked")
    public List<Double> getTemperature(WeatherRequestDTO weatherRequest) {
        String latitude = weatherRequest.getLocation().getLatitude().toString();
        String longitude = weatherRequest.getLocation().getLongitude().toString();

        LocalDate dateChosen = LocalDate.parse(weatherRequest.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long dayLimit = ChronoUnit.DAYS.between(LocalDate.now(), dateChosen) + 1;

        Map<String, Object> forecastMap =
                restTemplate.getForObject(weatherRequestUrl, Map.class, latitude, longitude, dayLimit);

        if (forecastMap != null && !forecastMap.isEmpty()) {
            Map<String, Object> hourlyMap = (Map<String, Object>) forecastMap.get(HOURLY);
            List<Double> tempsList = (List<Double>) hourlyMap.get(TEMP);
            tempsList = tempsList.subList(tempsList.size() - HOURS_IN_DAY, tempsList.size());
            OptionalDouble avgTemp = tempsList.stream().mapToDouble(a -> a).average();

            if (avgTemp.isPresent()) {
                Double avgTempRounded = Double.parseDouble(String.valueOf(Math.round(avgTemp.getAsDouble())));

                return List.of(avgTempRounded);
            } else {

                throw new InternalError("Unable to calculate daily average temperature for this location on "
                        + weatherRequest.getDate().replace("-", "."));
            }
        } else {

            throw new InternalError("Unable to determine temperature for this location on "
                    + weatherRequest.getDate().replace("-", "."));
        }
    }
}
