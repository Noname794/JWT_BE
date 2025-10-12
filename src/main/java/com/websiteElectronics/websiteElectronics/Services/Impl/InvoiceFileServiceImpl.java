package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Entities.OrderDetails;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Repositories.OrderDetailsRepository;
import com.websiteElectronics.websiteElectronics.Services.InvoiceFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@Service
public class InvoiceFileServiceImpl implements InvoiceFileService {

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Value("${invoice.folder.path:D:/websiteElectronics/invoices}")
    private String invoiceFolderPath;

    private final Object folderLock = new Object();

    @Override
    public String createInvoiceFile(Orders order) throws IOException {

        Path folderPath = Paths.get(invoiceFolderPath);

        synchronized (folderLock) {
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
        }

        String filename = "invoice_order_" + order.getId() + "_" + System.currentTimeMillis() + ".txt";
        String filePath = invoiceFolderPath + "/" + filename;


        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByOrderId_Id(order.getId());


        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        StringBuilder content = new StringBuilder();
        content.append("=====================================\n");
        content.append("         HÓA ĐƠN CHI TIẾT            \n");
        content.append("=====================================\n\n");

        content.append("Mã đơn hàng: ").append(order.getId()).append("\n");
        content.append("Ngày đặt: ").append(dateFormat.format(order.getOrderDate())).append("\n");
        content.append("Trạng thái: ").append(order.getStatus()).append("\n\n");

        content.append("-------------------------------------\n");
        content.append("THÔNG TIN KHÁCH HÀNG\n");
        content.append("-------------------------------------\n");
        content.append("Tên: ").append(order.getCustomer().getFirstName())
               .append(" ").append(order.getCustomer().getLastName()).append("\n");
        content.append("Email: ").append(order.getCustomer().getEmail()).append("\n");
        content.append("Số điện thoại: ").append(order.getCustomer().getPhoneNumber()).append("\n");
        content.append("Địa chỉ: ").append(order.getCustomer().getAddress()).append(", ")
               .append(order.getCustomer().getCity()).append(", ")
               .append(order.getCustomer().getState()).append("\n\n");

        content.append("-------------------------------------\n");
        content.append("CHI TIẾT ĐƠN HÀNG\n");
        content.append("-------------------------------------\n");

        int totalQuantity = 0;
        for (OrderDetails detail : orderDetailsList) {
            content.append("Sản phẩm: ").append(detail.getProductId().getName()).append("\n");
            content.append("  - Số lượng: ").append(detail.getQuantity()).append("\n");
            content.append("  - Đơn giá: ").append(currencyFormat.format(detail.getProductId().getPrice())).append("\n");
            content.append("  - Thành tiền: ").append(currencyFormat.format(detail.getQuantity() * detail.getProductId().getPrice())).append("\n\n");
            totalQuantity += detail.getQuantity();
        }

        content.append("-------------------------------------\n");
        content.append("Tổng số lượng: ").append(totalQuantity).append("\n");
        content.append("TỔNG TIỀN: ").append(currencyFormat.format(order.getTotalAmount())).append("\n");
        content.append("-------------------------------------\n\n");

        content.append("Phương thức thanh toán: ").append(order.getPaymentMethod().getName()).append("\n");
        content.append("Phương thức vận chuyển: ").append(order.getShippingMethod().getName()).append("\n\n");

        content.append("=====================================\n");
        content.append("   Cảm ơn quý khách đã mua hàng    \n");
        content.append("=====================================\n");

        Files.write(Paths.get(filePath), content.toString().getBytes(StandardCharsets.UTF_8));

        return filePath;
    }
}
