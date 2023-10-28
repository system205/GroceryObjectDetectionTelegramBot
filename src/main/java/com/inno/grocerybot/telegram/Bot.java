package com.inno.grocerybot.telegram;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.telegram.telegrambots.bots.*;
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class Bot extends TelegramLongPollingBot {
    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(Objects.requireNonNullElse(getBase64Photo(update), "empty empty empty").substring(0, 15));
    }

    private String getBase64Photo(Update update) {
        final PhotoSize photo = getPhoto(update);

        if (photo != null){
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());

            File telegramFile;
            try {
                telegramFile = execute(getFileMethod);
            } catch (TelegramApiException e) {
                System.err.println("Exception when getting file path of a photo");
                throw new RuntimeException(e);
            }

            final String filePath = telegramFile.getFilePath();
            System.out.println("File path: " + filePath);

            try {
                final java.io.File file = downloadFile(filePath);
                final byte[] content = Files.readAllBytes(file.toPath());
                return Base64.getEncoder().encodeToString(content);
            } catch (TelegramApiException | IOException e) {
                System.err.println("Exception when downloading a file");
                throw new RuntimeException(e);
            }
        }

        return null;
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
    public String getBotUsername() {
        return "GroceryBOt";
    }
}
