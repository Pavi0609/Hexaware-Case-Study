package com.hexaware.Test;

import com.hexaware.Dao.OrderProcessorRepository;
import com.hexaware.Service.OrderProcessorRepositoryImpl;
import com.hexaware.Entity.Customer;
import com.hexaware.Entity.Product;
import com.hexaware.Exception.CustomerNotFoundException;
import com.hexaware.Exception.ProductNotFoundException;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorRepositoryImplTest {

    private Connection connection;
    private OrderProcessorRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        repository = new OrderProcessorRepositoryImpl();
        initializeDatabase();
    }

    private void initializeDatabase() throws SQLException {
        Statement stmt = connection.createStatement();
        //-> Create tables
        stmt.execute("CREATE TABLE customers (customer_id INT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), password VARCHAR(255))");
        stmt.execute("CREATE TABLE products (product_id INT PRIMARY KEY, name VARCHAR(255), price DOUBLE, description VARCHAR(255), stock_quantity INT)");
        stmt.execute("CREATE TABLE cart (customer_id INT, product_id INT, quantity INT)");
        stmt.execute("CREATE TABLE orders (order_id INT AUTO_INCREMENT PRIMARY KEY, customer_id INT, total DOUBLE, address VARCHAR(255))");
        stmt.execute("CREATE TABLE order_items (order_id INT, product_id INT, quantity INT, price DOUBLE)");

        //-> Insert sample data
        stmt.execute("INSERT INTO customers VALUES (1, 'Alice', 'Alice@example.com','Alice123')");
        stmt.execute("INSERT INTO products VALUES (1, 'Phone', 500.0, 'Smartphone', 20)");
    }

    @Test
    void testCreateProductSuccess() throws SQLException {
        Product product = new Product(2, "Laptop", 1000.0, "Gaming Laptop", 15);
        boolean result = repository.createProduct(product);
        assertTrue(result, "Product should be created successfully.");
    }

    @Test
    void testAddToCartSuccess() throws SQLException, CustomerNotFoundException, ProductNotFoundException {
        Customer customer = new Customer(1, "Alice", "Alice@example.com", "Alice123");
        Product product = new Product(1, "Phone", 500.0, "Smartphone", 20);

        boolean result = repository.addToCart(customer, product, 2);
        assertTrue(result, "Product should be added to cart successfully.");
    }

    @Test
    void testPlaceOrderSuccess() throws SQLException, CustomerNotFoundException, ProductNotFoundException {
        Customer customer = new Customer(1, "Alice","Alice@example.com", "Alice123");
        Product product = new Product(1, "Phone", 500.0, "Smartphone", 20);

        Map<Product, Integer> productMap = new HashMap<>();
        productMap.put(product, 2);
        List<Map<Product, Integer>> productList = new ArrayList<>();
        productList.add(productMap);

        boolean result = repository.placeOrder(customer, productList, "123 Main Street");
        assertTrue(result, "Order should be placed successfully.");
    }

    @Test
    void testCustomerNotFoundExceptionThrown() {
        Customer customer = new Customer(999, "Bob", "Bob@example.com", "Bob456"); //-> Non-existent customer
        Product product = new Product(1, "Phone", 500.0, "Smartphone", 20);

        assertThrows(CustomerNotFoundException.class, () -> {
            repository.addToCart(customer, product, 1);
        }, "Should throw CustomerNotFoundException when customer not found.");
    }

    @Test
    void testProductNotFoundExceptionThrown() {
        Customer customer = new Customer(1, "Alice", "Alice@example.com", "Alice123");
        Product product = new Product(999, "Unknown Product", 0.0, "None", 0); //-> Non-existent product

        assertThrows(ProductNotFoundException.class, () -> {
            repository.addToCart(customer, product, 1);
        }, "Should throw ProductNotFoundException when product not found.");
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.createStatement().execute("DROP ALL OBJECTS");
        connection.close();
    }
}