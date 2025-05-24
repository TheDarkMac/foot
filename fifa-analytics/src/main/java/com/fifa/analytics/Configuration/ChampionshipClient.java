package com.fifa.analytics.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Configuration
public class ChampionshipClient {

    private WebClient webClient;

    private String apiKey = "user-fifa-api-key-abc456";

    public ChampionshipClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)) // optionnel, si les objets sont gros
                .build();
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .filter(addApiKeyHeader())
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private ExchangeFilterFunction addApiKeyHeader() {
        return (request, next) -> {
            return next.exchange(
                    ClientRequest.from(request)
                            .header("X-API-KEY", apiKey)  // Ajouter l'API Key dans chaque requête sortante
                            .build());
        };
    }
}
