package com.hexaware.Service;

import com.hexaware.Dao.OrderProcessorRepository;
import com.hexaware.Entity.Customer;
import com.hexaware.Entity.Product;
import com.hexaware.Exception.CustomerNotFoundException;
import com.hexaware.Exception.OrderNotFoundException;
import com.hexaware.Exception.ProductNotFoundException;
import com.hexaware.Util.DBConnUtil;
import com.hexaware.Util.DBPropertyUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderProcessorRepositoryImpl implements OrderProcessorRepository {
    private Connection connection;

    public OrderProcessorRepositoryImpl() {
    	 try {
             String connectionString = DBPropertyUtil.getConnectionString("db.properties");
             this.connection = DBConnUtil.getDBConnection(connectionString);
         } catch (SQLException e) {
             e.printStackTrace();
         }
    }

    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (product_name, price, description, stockQuantity) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setString(3, product.getDescription());
            statement.setInt(4, product.getStockQuantity());           
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, email, password) VALUES (?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPassword());          
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws ProductNotFoundException {
        // Check if product exists first
        if (!productExists(productId)) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        
        String sql = "DELETE FROM products WHERE product_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);        
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteCustomer(int customerId) throws CustomerNotFoundException {
        // Check if customer exists first
        if (!customerExists(customerId)) {
            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found.");
        }
        
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);            
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addToCart(Customer customer, Product product, int quantity) throws CustomerNotFoundException, ProductNotFoundException {
        //-> Check if customer exists or not
        if (!customerExists(customer.getCustomerId())) {
            throw new CustomerNotFoundException("Customer with ID " + customer.getCustomerId() + " not found.");
        }
        
        //-> Check if product exists or not
        if (!productExists(product.getProductId())) {
            throw new ProductNotFoundException("Product with ID " + product.getProductId() + " not found.");
        }
        
        //-> Check if product is already in cart or not
        String checkSql = "SELECT quantity FROM cart WHERE customer_id = ? AND product_id = ?";
        String updateSql = "UPDATE cart SET quantity = quantity + ? WHERE customer_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try {
            //-> Check if product is already in cart or not
            try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                checkStatement.setInt(1, customer.getCustomerId());
                checkStatement.setInt(2, product.getProductId());                
                ResultSet resultSet = checkStatement.executeQuery();
                
                if (resultSet.next()) {
                    //-> Product exists in cart, update quantity
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setInt(1, quantity);
                        updateStatement.setInt(2, customer.getCustomerId());
                        updateStatement.setInt(3, product.getProductId());                       
                        int rowsUpdated = updateStatement.executeUpdate();
                        return rowsUpdated > 0;
                    }
                } else {
                    //-> Product not in cart, insert new record
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                        insertStatement.setInt(1, customer.getCustomerId());
                        insertStatement.setInt(2, product.getProductId());
                        insertStatement.setInt(3, quantity);  
                        int rowsInserted = insertStatement.executeUpdate();
                        return rowsInserted > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeFromCart(Customer customer, Product product) throws CustomerNotFoundException, ProductNotFoundException {
        //-> Check if customer exists or not
        if (!customerExists(customer.getCustomerId())) {
            throw new CustomerNotFoundException("Customer with ID " + customer.getCustomerId() + " not found.");
        }
        
        //-> Check if product exists or not
        if (!productExists(product.getProductId())) {
            throw new ProductNotFoundException("Product with ID " + product.getProductId() + " not found.");
        }
        
        String sql = "DELETE FROM cart WHERE customer_id = ? AND product_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customer.getCustomerId());
            statement.setInt(2, product.getProductId());          
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Product> getAllFromCart(Customer customer) throws CustomerNotFoundException {
        //-> Check if customer exists or not
        if (!customerExists(customer.getCustomerId())) {
            throw new CustomerNotFoundException("Customer with ID " + customer.getCustomerId() + " not found.");
        }
        
        List<Product> cartProducts = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.price, p.description, p.stockQuantity, c.quantity " +
                     "FROM products p JOIN cart c ON p.product_id = c.product_id " +
                     "WHERE c.customer_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customer.getCustomerId());      
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Product product = new Product();
                product.setProductId(resultSet.getInt("product_id"));
                product.setName(resultSet.getString("product_name"));
                product.setPrice(resultSet.getDouble("price"));
                product.setDescription(resultSet.getString("description"));
                product.setStockQuantity(resultSet.getInt("stockQuantity"));
                
                cartProducts.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }        
        return cartProducts;
    }

    @Override
    public boolean placeOrder(Customer customer, List<Map<Product, Integer>> productsWithQuantities, String shippingAddress) throws CustomerNotFoundException, ProductNotFoundException {
        //-> Check if customer exists or not
        if (!customerExists(customer.getCustomerId())) {
            throw new CustomerNotFoundException("Customer with ID " + customer.getCustomerId() + " not found.");
        }
        
        //-> Calculate total price
        double totalPrice = 0;
        for (Map<Product, Integer> map : productsWithQuantities) {
            for (Map.Entry<Product, Integer> entry : map.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                
                //-> Check if product exists and has sufficient stock
                if (!productExists(product.getProductId())) {
                    throw new ProductNotFoundException("Product with ID " + product.getProductId() + " not found.");
                }
                
                //-> Get current stock
                int currentStock = 0;
				try {
					currentStock = getProductStock(product.getProductId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                if (currentStock < quantity) {
                    throw new IllegalArgumentException("Insufficient stock for product ID " + product.getProductId());
                }          
                totalPrice += product.getPrice() * quantity;
            }
        }
        
        //-> Start the transaction
        try {
            connection.setAutoCommit(false);
            
            // 1. Create order record
            String orderSql = "INSERT INTO orders (customer_id, total_price, shipping_address) VALUES (?, ?, ?)";
            int orderId;
            
            try (PreparedStatement orderStatement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStatement.setInt(1, customer.getCustomerId());
                orderStatement.setDouble(2, totalPrice);
                orderStatement.setString(3, shippingAddress);          
                orderStatement.executeUpdate();
                
                //-> Get generated order ID
                ResultSet generatedKeys = orderStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get order ID.");
                }
            }
            
            // 2. Create order items and update product stock
            String orderItemSql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
            String updateStockSql = "UPDATE products SET stockQuantity = stockQuantity - ? WHERE product_id = ?";
            
            for (Map<Product, Integer> map : productsWithQuantities) {
                for (Map.Entry<Product, Integer> entry : map.entrySet()) {
                    Product product = entry.getKey();
                    int quantity = entry.getValue();
                    
                    //-> Add order item
                    try (PreparedStatement orderItemStatement = connection.prepareStatement(orderItemSql)) {
                        orderItemStatement.setInt(1, orderId);
                        orderItemStatement.setInt(2, product.getProductId());
                        orderItemStatement.setInt(3, quantity);          
                        orderItemStatement.executeUpdate();
                    }
                    
                    //-> Update product stock
                    try (PreparedStatement updateStockStatement = connection.prepareStatement(updateStockSql)) {
                        updateStockStatement.setInt(1, quantity);
                        updateStockStatement.setInt(2, product.getProductId());
                        updateStockStatement.executeUpdate();
                    }
                }
            }
            
            // 3. Clear the cart for this customer
            String clearCartSql = "DELETE FROM cart WHERE customer_id = ?";
            try (PreparedStatement clearCartStatement = connection.prepareStatement(clearCartSql)) {
                clearCartStatement.setInt(1, customer.getCustomerId());
                clearCartStatement.executeUpdate();
            }
            
            //-> Commit transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Map<Product, Integer>> getOrdersByCustomer(int customerId) throws CustomerNotFoundException, OrderNotFoundException {
        //-> Check if customer exists or not
        if (!customerExists(customerId)) {
            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found.");
        }
        
        List<Map<Product, Integer>> orders = new ArrayList<>();
        
        //-> First get all orders for the customer
        String ordersSql = "SELECT order_id FROM orders WHERE customer_id = ?";
        List<Integer> orderIds = new ArrayList<>();
        
        try (PreparedStatement ordersStatement = connection.prepareStatement(ordersSql)) {
            ordersStatement.setInt(1, customerId);  
            ResultSet ordersResult = ordersStatement.executeQuery();
            
            while (ordersResult.next()) {
                orderIds.add(ordersResult.getInt("order_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if (orderIds.isEmpty()) {
            throw new OrderNotFoundException("No orders found for customer ID " + customerId);
        }
        
        //-> For each order, get the products and quantities
        String itemsSql = "SELECT p.product_id, p.product_name, p.price, p.description, p.stockQuantity, oi.quantity " +
                         "FROM order_items oi JOIN products p ON oi.product_id = p.product_id " +
                         "WHERE oi.order_id = ?";
        
        for (int orderId : orderIds) {
            Map<Product, Integer> orderItems = new HashMap<>();
            
            try (PreparedStatement itemsStatement = connection.prepareStatement(itemsSql)) {
                itemsStatement.setInt(1, orderId);  
                ResultSet itemsResult = itemsStatement.executeQuery();
                
                while (itemsResult.next()) {
                    Product product = new Product();
                    product.setProductId(itemsResult.getInt("product_id"));
                    product.setName(itemsResult.getString("product_name"));
                    product.setPrice(itemsResult.getDouble("price"));
                    product.setDescription(itemsResult.getString("description"));
                    product.setStockQuantity(itemsResult.getInt("stockQuantity"));                
                    int quantity = itemsResult.getInt("quantity");          
                    orderItems.put(product, quantity);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            if (!orderItems.isEmpty()) {
                orders.add(orderItems);
            }
        }   
        return orders;
    }
    
    //-> Helper methods
    private boolean customerExists(int customerId) {
        String sql = "SELECT 1 FROM customers WHERE customer_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);   
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean productExists(int productId) {
        String sql = "SELECT 1 FROM products WHERE product_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);     
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private int getProductStock(int productId) throws SQLException {
        String sql = "SELECT stockQuantity FROM products WHERE product_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("stockQuantity");
            }
        }
        return 0;
    }
}