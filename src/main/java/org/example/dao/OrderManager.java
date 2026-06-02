package org.example.dao;

import org.example.db.DBUtility;
import org.example.entity.Customer;
import org.example.entity.Order;
import org.example.entity.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    public boolean isExitProductName(String productName){
        boolean flag = false;
        Connection con;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("SELECT COUNT(*) FROM products WHERE name = ?");
            pstmt.setString(1, productName);
            rs = pstmt.executeQuery();
            if(rs.next()){
                flag = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,pstmt,con);
        }
        return flag;
    }

    public boolean isCustomerExit(int customerId){
        boolean flag = false;
        Connection con;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("SELECT COUNT(*) FROM customers WHERE id = ?");
            pstmt.setInt(1, customerId);
            rs = pstmt.executeQuery();
            if(rs.next()){
                flag = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,pstmt,con);
        }
        return flag;
    }

    public boolean isEmailExist(String email){
        boolean flag = false;
        Connection con;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("SELECT COUNT(*) FROM customers WHERE email = ? AND id != ?");
            pstmt.setString(1, email);
            pstmt.setInt(2, 0);
            rs = pstmt.executeQuery();
            if(rs.next()){
                flag = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,pstmt,con);
        }
        return flag;
    }

    public double getProductPrice(int productId){
        double price = -1;
        Connection con;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("SELECT price FROM products WHERE id = ?");
            pstmt.setInt(1, productId);
            rs = pstmt.executeQuery();
            if(rs.next()){
                price = rs.getDouble("price");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,pstmt,con);
        }
        return price;
    }
    public boolean addProduct(Product product){
        boolean flag = false;
        if (isExitProductName(product.getName())){
            System.out.println("Lỗi: Tên sản phẩm [" + product.getName() + "] đã tồn tại!");
            return false;
        }
        Connection con;
        PreparedStatement pstmt = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("INSERT INTO products(name, price) VALUES (?, ?)");
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.executeUpdate();
            flag = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(null,pstmt,con);
        }
        return flag;
    }

    public boolean updateCustomer(int customerId, Customer customer){
        boolean flag = false;
        if (!isCustomerExit(customerId)){
            System.out.println("Lỗi: Không tìm thấy khách hàng ID = " + customerId + " để cập nhật!");
            return false;
        }

        if (isEmailExist(customer.getEmail())){
            System.out.println("Lỗi: Email [" + customer.getEmail() + "] đã được sử dụng bởi một khách hàng khác!");
            return false;
        }

        Connection con;
        PreparedStatement pstmt = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("UPDATE customers SET name = ?, email = ? WHERE id = ?");
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setInt(3, customerId);
            pstmt.executeUpdate();
            flag = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(null,pstmt,con);
        }

        return flag;
    }

    public boolean createOrder(Order order){
        boolean flag = false;
        if (!isCustomerExit(order.getCustomerId().getId())){
            System.out.println("Lỗi: Khách hàng ID = " + order.getCustomerId().getId() + " không tồn tại!");
            return false;
        }

        double price = getProductPrice(order.getProductId().getId());
        if (price == -1) {
            System.out.println("Lỗi: Sản phẩm ID = " + order.getProductId().getId() + " không tồn tại!");
            return false;
        }


        Connection con;
        PreparedStatement pstmt = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("INSERT INTO orders(customer_id, product_id, order_date, quantity, total_amount) VALUES (?, ?, ?, ?, ?)");            pstmt.setInt(1, order.getCustomerId().getId());
            pstmt.setInt(2, order.getProductId().getId());
            pstmt.setDate(3, Date.valueOf(order.getOrderDate()));
            pstmt.setInt(4, order.getQuantity());
            pstmt.setDouble(5, order.getQuantity() * price);
            pstmt.executeUpdate();
            flag = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(null,pstmt,con);
        }
        return flag;
    }

    public List<Order> listAllOrders(){
        List<Order> orders = new ArrayList<>();
        Connection con;
        Statement stmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT o.id AS order_id, o.order_date, o.quantity, o.total_amount, " +
                    "c.id AS customer_id, c.name AS customer_name, c.email AS customer_email, " +
                    "p.id AS product_id, p.name AS product_name, p.price AS product_price " +
                    "FROM orders o " +
                    "INNER JOIN customers c ON o.customer_id = c.id " +
                    "INNER JOIN products p ON o.product_id = p.id ORDER BY o.id DESC");
            while (rs.next()){
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_email")
                );

                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("product_price")
                );

                Order order = new Order(
                        rs.getInt("order_id"),
                        customer,
                        product,
                        rs.getDate("order_date").toLocalDate(),
                        rs.getInt("quantity"),
                        rs.getDouble("total_amount")
                );

                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,stmt,con);
        }
        return orders;
    }

    public List<Order> getOrdersByCustomer(int customerId){
        List<Order> orders = new ArrayList<>();
        Connection con;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("SELECT o.id AS order_id, o.order_date, o.quantity, o.total_amount, " +
                    "c.id AS customer_id, c.name AS customer_name, c.email AS customer_email, " +
                    "p.id AS product_id, p.name AS product_name, p.price AS product_price " +
                    "FROM orders o " +
                    "INNER JOIN customers c ON o.customer_id = c.id " +
                    "INNER JOIN products p ON o.product_id = p.id " +
                    "WHERE o.customer_id = ? ORDER BY o.id DESC");
            pstmt.setInt(1, customerId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_email")
                );

                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("product_price")
                );

                Order order = new Order(
                        rs.getInt("order_id"),
                        customer,
                        product,
                        rs.getDate("order_date").toLocalDate(),
                        rs.getInt("quantity"),
                        rs.getDouble("total_amount")
                );

                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,pstmt,con);
        }
        return orders;
    }

    public boolean isCustomerExist(int id) {
        boolean flag = false;
        Connection con;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        con = DBUtility.openConnection();
        try {
            pstmt = con.prepareStatement("SELECT COUNT(*) FROM customers WHERE id = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                flag = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtility.closeConnection(rs,pstmt,con);
        }
        return flag;
    }
}
