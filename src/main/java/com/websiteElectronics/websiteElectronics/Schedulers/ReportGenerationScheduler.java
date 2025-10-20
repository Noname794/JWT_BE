package com.websiteElectronics.websiteElectronics.Schedulers;

import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportGenerationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerationScheduler.class);

    private final OrdersRepository ordersRepository;

    private final CustomersRepository customersRepository;

    private final Object folderLock = new Object();

    @Autowired
    public ReportGenerationScheduler(OrdersRepository ordersRepository, CustomersRepository customersRepository) {
        this.ordersRepository = ordersRepository;
        this.customersRepository = customersRepository;
    }

    @Value("${report.folder.path:D:/websiteElectronics/reports}")
    private String reportFolderPath;

    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailyRevenue() throws IOException {
        logger.info("Generating daily revenue report...");

        try {
            Path folderPath = Paths.get(reportFolderPath);

            synchronized (folderLock) {
                if (!Files.exists(folderPath)) {
                    Files.createDirectories(folderPath);
                }
            }

            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Orders> toDayOrders = ordersRepository.findAll().stream()
                    .filter(order -> order.getOrderDate().isAfter(startOfDay) && order.getOrderDate().isBefore(endOfDay))
                    .toList();

            int totalOrders = toDayOrders.size();
            double totalRevenue = toDayOrders.stream()
                    .mapToDouble(Orders::getTotalAmount)
                    .sum();

            long shipped = toDayOrders.stream()
                    .filter(order -> order.getStatus().equals("Shipped"))
                    .count();

            long delivery = toDayOrders.stream()
                    .filter(order -> order.getStatus().equals("Delivery"))
                    .count();

            String fileName = String.format("daily_revenue_%s.txt",
                    today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            String filePath = folderPath + "/" + fileName;

            try(FileWriter fileWriter = new FileWriter(filePath)) {
                fileWriter.write("===========================================\n");
                fileWriter.write("              BÁO CÁO HÀNG NGÀY           \n");
                fileWriter.write("===========================================\n");
                fileWriter.write("Ngày: " + today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
                fileWriter.write("Tong so don hang: " + totalOrders + "\n");
                fileWriter.write("Tong doanh thu: " + totalRevenue + "\n");
                fileWriter.write("Tong so don hang da giao: " + shipped + "\n");
                fileWriter.write("Tong so don hang dang van chuyen: " + delivery + "\n");
                fileWriter.write("===========================================\n");

                if (totalOrders > 0) {
                    fileWriter.write("Tong doanh thu trung binh: " + (totalRevenue / totalOrders) + "\n");
                }

                fileWriter.write("===========================================\n");
                fileWriter.write("Danh sach don hang:\n");
                fileWriter.write("===========================================\n");

                for (Orders order : toDayOrders) {
                    fileWriter.write("Mã đơn hàng: " + order.getId() + "\n");
                    fileWriter.write("Khách hàng: " +order.getCustomer().getFirstName()+order.getCustomer().getLastName()+"\n");
                    fileWriter.write("Ngày đặt: " + order.getOrderDate() + "\n");
                    fileWriter.write("Trạng thái: " + order.getStatus() + "\n");
                    fileWriter.write("Phuong thức thanh toán: " + order.getPaymentMethod().getName() + "\n");
                    fileWriter.write("Phuong thức giao hàng: " + order.getShippingMethod().getName() + "\n");
                    fileWriter.write("Tong doanh thu: " + order.getTotalAmount() + "\n");
                    fileWriter.write("===========================================\n");
                }
            }catch (IOException e) {
                logger.error("Error writing to file: {}", filePath, e);
            }
        }catch (Exception e) {
            logger.error("Error creating report folder: {}", reportFolderPath, e);
        }

    }

}
