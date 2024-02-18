package electrocar.configuration;

import com.google.gson.Gson;
import electrocar.dto.route.OpenRouteServiceRequestDTO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MainConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("dd.MM.yyyy");
    }

    @Bean
    public OpenRouteServiceRequestDTO openRouteServiceRequestDTO() {
        return new OpenRouteServiceRequestDTO(new ArrayList<>(), false, "km", false, "shortest", false);
    }
}
