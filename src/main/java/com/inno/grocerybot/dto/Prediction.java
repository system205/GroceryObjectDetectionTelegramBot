package com.inno.grocerybot.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
public class Prediction {
    private double confidence;
    @JsonProperty("class")
    private String className;
    private int classId;
}
