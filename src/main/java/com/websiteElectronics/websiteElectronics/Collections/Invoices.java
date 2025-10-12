package com.websiteElectronics.websiteElectronics.Collections;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Invoices")
public class Invoices {

    @Id
    private String id;
    private Long orderId;
    private Long customerId;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
}
