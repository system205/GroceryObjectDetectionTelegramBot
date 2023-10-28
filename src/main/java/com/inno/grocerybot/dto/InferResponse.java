package com.inno.grocerybot.dto;

import lombok.*;

import java.util.*;

@Data
public class InferResponse {
    private double time;
    private List<Prediction> predictions;
}
