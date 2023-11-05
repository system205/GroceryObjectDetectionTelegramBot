package com.inno.grocerybot.telegram;

import com.inno.grocerybot.dto.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.*;
import org.springframework.core.env.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.*;
import org.telegram.telegrambots.bots.*;
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.*;
import reactor.core.publisher.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
    private final Environment env;
    private final WebClient webClient;

    public Bot(@Value("${bot.token}") String botToken, WebClient webClient, Environment env) {
        super(botToken);
        this.webClient = webClient;
        this.env = env;
    }

    private static PhotoSize getPhoto(Update update) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            List<PhotoSize> photos = update.getMessage().getPhoto();

            // Fetch the bigger photo
            return Objects.requireNonNull(photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null),
                "Photo can not be null");
        }

        // Not found
        return null;
    }

    @Override
    public void onUpdateReceived(Update update) {
        final long startTime = System.currentTimeMillis();
        final String base64Photo = getBase64Photo(update);

        if (base64Photo == null) {
            return;
        }

        String text;
        try {
            final InferResponse response = getClasses(base64Photo);
            final Set<String> classes = response.getClasses();
            log.info("Classes: {}", classes);
            final String classesAsString = classes.stream().reduce((s, s2) -> s + ", " + s2).orElse("");
            text = classesAsString.isBlank() ? "I didn't recognize anything(" : "Recognized classes: " + classesAsString;
            final long endTime = System.currentTimeMillis();
            text += String.format("%nInference time: %dms %nWhole process took: %dms",
                (long) Math.ceil(response.getTime()), endTime - startTime);
        } catch (WebClientRequestException e) {
            log.info("Error with WebClient", e);
            text = "Sorry. The server is down. Contact admins";
        }

        sendMessage(update.getMessage().getChatId(), text);

    }

    private InferResponse getClasses(String base64Photo) {
        final InferRequest request = new InferRequest("base64", base64Photo, env);

        Mono<List<InferResponse>> inferResponseMono = webClient
            .post()
            .uri("infer/object_detection")
            .body(Mono.just(request), InferRequest.class)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<>() {
            });

        InferResponse response = Objects.requireNonNull(inferResponseMono.block()).get(0);
        log.info("Found {} predictions in response. Took {} ms", response.getPredictions().size(), response.getTime());
        response.setClasses(response.getPredictions().stream().map(Prediction::getClassName).collect(Collectors.toSet()));

        return response;
    }

    private String getBase64Photo(Update update) {
        final PhotoSize photo = getPhoto(update);

        if (photo != null) {
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());

            File telegramFile;
            try {
                telegramFile = execute(getFileMethod);
            } catch (TelegramApiException e) {
                log.error("Exception when getting file path of a photo", e);
                throw new IllegalStateException(e);
            }

            final String filePath = telegramFile.getFilePath();
            log.info("File path: {}", filePath);

            try {
                final java.io.File file = downloadFile(filePath);
                final byte[] content = Files.readAllBytes(file.toPath());
                return Base64.getEncoder().encodeToString(content);
            } catch (TelegramApiException | IOException e) {
                log.error("Exception when downloading a file", e);
                throw new IllegalStateException(e);
            }
        }

        return null;
    }

    private void sendMessage(Long chatId, String text) {
        final SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error when sending message", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "GroceryBot";
    }
}
