package com.inno.grocerybot.dto;


import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.springframework.core.env.*;

import java.util.*;

@Data
public class InferRequest {
    private List<Image> image = new ArrayList<>(1);
    @JsonProperty("model_id")
    private String modelId;
    @JsonProperty("model_type")
    private String modelType;
    @JsonProperty("api_key")
    private String apiKey;

    private float confidence = 0.7f;
    @JsonProperty("iou_threshold")
    private float iouThreshold = 0.5f;

    public InferRequest(String imageType, String imageValue, Environment env) {
        image.add(new Image(imageType, imageValue));
        this.modelId = env.getProperty("infer.request.model-id");
        this.modelType = env.getRequiredProperty("infer.request.model-type");
        this.apiKey = env.getProperty("infer.request.api-key");
    }


}
