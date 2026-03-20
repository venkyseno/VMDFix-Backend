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
    // Frontend passes the exact name and image it displayed — always trust these
    private String serviceName;
    private String serviceImageUrl;
    private String bookingAddress;
    // "our" or "quick" — tells backend which table to look up if name is missing
    private String serviceType;
}
