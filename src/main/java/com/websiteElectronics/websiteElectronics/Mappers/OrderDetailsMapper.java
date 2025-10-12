package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.OrderDetailsDto;
import com.websiteElectronics.websiteElectronics.Entities.OrderDetails;

public class OrderDetailsMapper {
    public static OrderDetailsDto toDto(OrderDetails entity){
        return new OrderDetailsDto(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity()
        );
    }

    public static OrderDetails toEntity(OrderDetailsDto dto){
        return new OrderDetails(
                dto.getId(),
                dto.getOrderId(),
                dto.getProductId(),
                dto.getQuantity()
        );
    }
}
