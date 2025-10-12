package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Collections.Invoices;
import com.websiteElectronics.websiteElectronics.Dtos.InvoicesDto;

public class InvoicesMapper {

    public static InvoicesDto mapToDto(Invoices invoices) {
        return new InvoicesDto(
                invoices.getId(),
                invoices.getOrderId(),
                invoices.getCustomerId(),
                invoices.getFileUrl(),
                invoices.getCreatedAt(),
                invoices.getExpireAt()
        );
    }

    public static Invoices mapToEntity(InvoicesDto invoicesDto) {
        return new Invoices(
                invoicesDto.getId(),
                invoicesDto.getOrderId(),
                invoicesDto.getCustomerId(),
                invoicesDto.getFileUrl(),
                invoicesDto.getCreatedAt(),
                invoicesDto.getExpireAt()
        );
    }

}
