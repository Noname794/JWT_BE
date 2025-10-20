package com.websiteElectronics.websiteElectronics.Controllers.ExportToExcel;

import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.websiteElectronics.websiteElectronics.Dtos.OrdersDto;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OrdersExport {
    public static void exportToCsv(List<OrdersDto> orders, OutputStream os) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        StatefulBeanToCsv<OrdersDto> beanToCsv = new StatefulBeanToCsvBuilder<OrdersDto>(writer)
                .withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
                .build();
        beanToCsv.write(orders);
        writer.flush();
    }
}
