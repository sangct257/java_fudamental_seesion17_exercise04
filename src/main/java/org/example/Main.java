package org.example;


import org.example.dao.OrderManager;
import org.example.entity.Customer;
import org.example.entity.Order;
import org.example.entity.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final OrderManager orderManager = new OrderManager();
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("\n======= MENU QUẢN LÝ ĐƠN HÀNG CỬA HÀNG =======");
        System.out.println("1. Thêm sản phẩm mới");
        System.out.println("2. Cập nhật thông tin khách hàng");
        System.out.println("3. Tạo đơn hàng mới");
        System.out.println("4. Hiển thị danh sách đơn hàng");
        System.out.println("5. Tìm kiếm đơn hàng theo mã khách hàng");
        System.out.println("6. Thoát");
        System.out.print("Lựa chọn của bạn (1-6): ");

        int choice = inputInt();

        switch (choice) {
            case 1: handleAddProduct(); break;
            case 2: handleUpdateCustomer(); break;
            case 3: handleCreateOrder(); break;
            case 4: handleListAllOrders(); break;
            case 5: handleGetOrdersByCustomer(); break;
            case 6:
                System.out.println("Thoát chương trình.");
                System.exit(0);
            default:
                System.out.println("Lựa chọn không hợp lệ! Vui lòng chọn từ 1 đến 6.");
        }
    }

    private static int inputInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Lỗi: Phải nhập số nguyên hợp lệ! Vui lòng nhập lại: ");
            }
        }
    }

    // Bẫy lỗi nhập số thực (Dùng cho giá sản phẩm)
    private static double inputDouble() {
        while (true) {
            try {
                double val = Double.parseDouble(scanner.nextLine().trim());
                if (val <= 0) {
                    System.out.print("Lỗi: Giá trị phải lớn hơn 0! Vui lòng nhập lại: ");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.print("Lỗi: Định dạng số thực không đúng! Vui lòng nhập lại: ");
            }
        }
    }

    // Bẫy lỗi bỏ trống chuỗi văn bản
    private static String inputString(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Lỗi: Trường này bắt buộc, không được để trống!");
            } else {
                return input;
            }
        }
    }

    private static void handleAddProduct() {
        System.out.println("\n--- THÊM SẢN PHẨM MỚI ---");
        String name = inputString("Nhập tên sản phẩm: ");
        System.out.print("Nhập giá sản phẩm: ");
        double price = inputDouble();

        Product product = new Product(name, price);
        if (orderManager.addProduct(product)) {
            System.out.println("Thành công: Đã thêm sản phẩm vào hệ thống!");
        }
    }

    private static void handleUpdateCustomer() {
        System.out.println("\n--- CẬP NHẬT THÔNG TIN KHÁCH HÀNG ---");
        System.out.print("Nhập mã ID khách hàng cần sửa: ");
        int id = inputInt();

        // Check nhanh nếu ID có tồn tại mới cho người dùng gõ tiếp thông tin mới
        if (!orderManager.isCustomerExist(id)) {
            System.out.println("Thất bại: Không tìm thấy khách hàng nào mang ID = " + id);
            return;
        }

        String name = inputString("Nhập họ và tên mới: ");
        String email = inputString("Nhập địa chỉ Email mới: ");

        Customer customer = new Customer(name, email);
        if (orderManager.updateCustomer(id, customer)) {
            System.out.println("Thành công: Đã cập nhật dữ liệu khách hàng!");
        }
    }

    private static void handleCreateOrder() {
        System.out.println("\n--- TẠO ĐƠN HÀNG MỚI ---");
        System.out.print("Nhập mã ID khách hàng mua: ");
        int customerId = inputInt();
        System.out.print("Nhập mã ID sản phẩm muốn mua: ");
        int productId = inputInt();
        System.out.print("Nhập số lượng sản phẩm: ");
        int quantity = inputInt();

        if (quantity <= 0) {
            System.out.println("Lỗi: Số lượng đặt mua sản phẩm phải lớn hơn 0!");
            return;
        }

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);

        Product mockProduct = new Product();
        mockProduct.setId(productId);

        Order order = new Order();
        order.setCustomerId(mockCustomer);
        order.setProductId(mockProduct);
        order.setQuantity(quantity);
        order.setOrderDate(LocalDate.now());

        if (orderManager.createOrder(order)) {
            System.out.println("Thành công: Đơn hàng đã được ghi nhận trên hệ thống!");
        }
    }

    private static void handleListAllOrders() {
        System.out.println("\n--- DANH SÁCH TOÀN BỘ ĐƠN HÀNG TRÊN HỆ THỐNG ---");
        List<Order> list = orderManager.listAllOrders();
        printOrderTable(list);
    }

    private static void handleGetOrdersByCustomer() {
        System.out.println("\n--- TÌM KIẾM ĐƠN HÀNG THEO MÃ KHÁCH HÀNG ---");
        System.out.print("Nhập mã ID khách hàng cần truy vấn: ");
        int customerId = inputInt();

        List<Order> list = orderManager.getOrdersByCustomer(customerId);
        printOrderTable(list);
    }

    private static void printOrderTable(List<Order> list) {
        if (list.isEmpty()) {
            System.out.println("ℹKhông tìm thấy bất kỳ dữ liệu đơn hàng nào thỏa mãn yêu cầu.");
            return;
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-8s | %-22s | %-20s | %-12s | %-8s | %-15s\n",
                "MÃ ĐƠN", "TÊN KHÁCH HÀNG", "SẢN PHẨM MUA", "NGÀY ĐẶT", "SỐ LƯỢNG", "TỔNG TIỀN (VND)");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");

        for (Order o : list) {
            System.out.printf("%-8d | %-22s | %-20s | %-12s | %-8d | %-15.2f\n",
                    o.getId(),
                    o.getCustomerId().getName(),
                    o.getProductId().getName(),
                    o.getOrderDate().toString(),
                    o.getQuantity(),
                    o.getTotalAmount()
            );
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
    }
}