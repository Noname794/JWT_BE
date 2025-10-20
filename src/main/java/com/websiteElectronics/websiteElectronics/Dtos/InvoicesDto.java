package com.websiteElectronics.websiteElectronics.Dtos;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoicesDto {

    private String id;
    private Long orderId;
    private Long customerId;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
}
