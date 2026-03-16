package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCaseRequest {

    private Long serviceId;
    private String description;
    private String customerPhone;
    private Long assistedByUserId;
    private String attachmentUrl;
    // Optional: frontend can pass the service name directly
    private String serviceName;
    private String serviceImageUrl;
    private String bookingAddress;
}
