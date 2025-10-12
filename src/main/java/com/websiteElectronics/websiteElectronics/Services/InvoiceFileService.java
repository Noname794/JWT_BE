package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Entities.Orders;

import java.io.IOException;


public interface InvoiceFileService {
    String createInvoiceFile(Orders order) throws IOException;
}
