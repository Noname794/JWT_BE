package com.websiteElectronics.websiteElectronics.Controllers.ExportToExcel;

import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.websiteElectronics.websiteElectronics.Dtos.ProductsDto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProductsExport {
    public static void exportToCsv(List<ProductsDto> orders, OutputStream os) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        StatefulBeanToCsv<ProductsDto> beanToCsv = new StatefulBeanToCsvBuilder<ProductsDto>(writer)
                .withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
                .build();
        beanToCsv.write(orders);
        writer.flush();
    }
}
