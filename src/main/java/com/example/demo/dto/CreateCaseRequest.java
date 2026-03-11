package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCaseRequest {

    private Long serviceId;
    private String description;
    private String customerPhone;
    private Long assistedByUserId;
    private String attachmentUrl;
}
