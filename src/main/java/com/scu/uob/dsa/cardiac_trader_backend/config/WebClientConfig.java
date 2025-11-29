package com.scu.uob.dsa.cardiac_trader_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;

import java.time.Duration;

/**
 * WebClient Configuration for external API calls
 */
@Configuration
public class WebClientConfig {

    @Value("${spring.web.client.timeout:5000}")
    private int timeout;

    @Value("${spring.web.client.connection-timeout:3000}")
    private int connectionTimeout;

    @Bean
    @SuppressWarnings("null")
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));  // NOSONAR - httpClient is non-null
    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}

