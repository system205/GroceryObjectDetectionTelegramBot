package com.inno.grocerybot;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.*;
import org.telegram.telegrambots.meta.*;
import org.telegram.telegrambots.meta.exceptions.*;
import org.telegram.telegrambots.meta.generics.*;
import org.telegram.telegrambots.updatesreceivers.*;

@SpringBootApplication
@Slf4j
public class GroceryBotApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GroceryBotApplication.class, args);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            LongPollingBot bot = context.getBean(LongPollingBot.class);
            botsApi.registerBot(bot);
            log.info("Bot is registered successfully");
        } catch (TelegramApiException e) {
            log.error("Can't register a bot. ", e);
        }
    }

    @Bean
    public WebClient webClient(@Value("${web-client.base-url:http://localhost:9001}") String baseUrl){
        return WebClient.builder().baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
