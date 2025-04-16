package com.hexaware.Main;

import com.hexaware.Dao.OrderProcessorRepository;
import com.hexaware.Service.OrderProcessorRepositoryImpl;
import com.hexaware.Entity.Customer;
import com.hexaware.Entity.Product;
import com.hexaware.Exception.CustomerNotFoundException;
import com.hexaware.Exception.OrderNotFoundException;
import com.hexaware.Exception.ProductNotFoundException;

import java.util.*;

public class EcomApp {
    private static OrderProcessorRepository orderProcessor;
    private static Scanner scanner;

    public static void main(String[] args) {
        orderProcessor = new OrderProcessorRepositoryImpl();
        scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("\n===== E-Commerce Application System =====");
            System.out.println("1. Register Customer");
            System.out.println("2. Create Product");
            System.out.println("3. Delete Product");
            System.out.println("4. Add to Cart");
            System.out.println("5. View Cart");
            System.out.println("6. Place Order");
            System.out.println("7. View Customer Orders");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); //-> Consume newline

            try {
                switch (choice) {
                    case 1:
                        registerCustomer();
                        break;
                    case 2:
                        createProduct();
                        break;
                    case 3:
                        deleteProduct();
                        break;
                    case 4:
                        addToCart();
                        break;
                    case 5:
                        viewCart();
                        break;
                    case 6:
                        placeOrder();
                        break;
                    case 7:
                        viewCustomerOrders();
                        break;
                    case 8:
                        running = false;
                        System.out.println("Exiting the system. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (CustomerNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (ProductNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (OrderNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    private static void registerCustomer() {
        System.out.println("\n--- Register New Customer ---");
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Customer customer = new Customer(name, email, password);
        
        boolean success = orderProcessor.createCustomer(customer);

        if (success) {
            System.out.println("Customer registered successfully!");
        } else {
            System.out.println("Failed to register customer.");
        }
    }

    private static void createProduct() {
        System.out.println("\n--- Create New Product ---");
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();

        System.out.print("Enter price: ");
        double price = scanner.nextDouble();
        scanner.nextLine(); //-> Declares newline

        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        System.out.print("Enter stock quantity: ");
        int stockQuantity = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        Product product = new Product(name, price, description, stockQuantity);
        
        boolean success = orderProcessor.createProduct(product);

        if (success) {
            System.out.println("Product created successfully!");
        } else {
            System.out.println("Failed to create product.");
        }
    }

    private static void deleteProduct() throws ProductNotFoundException {
        System.out.println("\n--- Delete Product ---");
        System.out.print("Enter product ID to delete: ");
        int productId = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline
        
        boolean success = orderProcessor.deleteProduct(productId);

        if (success) {
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Failed to delete product.");
        }
    }

    private static void addToCart() throws CustomerNotFoundException, ProductNotFoundException {
        System.out.println("\n--- Add Product to Cart ---");
        System.out.print("Enter customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        System.out.print("Enter product ID: ");
        int productId = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        Customer customer = new Customer();
        customer.setCustomerId(customerId);

        Product product = new Product();
        product.setProductId(productId);
        
        boolean success = orderProcessor.addToCart(customer, product, quantity);

        if (success) {
            System.out.println("Product added to cart successfully!");
        } else {
            System.out.println("Failed to add product to cart.");
        }
    }

    private static void viewCart() throws CustomerNotFoundException {
        System.out.println("\n--- View Cart ---");
        System.out.print("Enter customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        Customer customer = new Customer();
        customer.setCustomerId(customerId);

        List<Product> cartProducts = orderProcessor.getAllFromCart(customer);

        if (cartProducts.isEmpty()) {
            System.out.println("Your cart is empty.");
        } else {
            System.out.println("\nProducts in your cart:");
            for (Product product : cartProducts) {
                System.out.println(product);
            }
        }
    }

    private static void placeOrder() throws CustomerNotFoundException, ProductNotFoundException {
        System.out.println("\n--- Place Order ---");
        System.out.print("Enter customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        Customer customer = new Customer();
        customer.setCustomerId(customerId);

        //-> Get products from cart
        List<Product> cartProducts = orderProcessor.getAllFromCart(customer);
        
        if (cartProducts.isEmpty()) {
            System.out.println("Your cart is empty. Cannot place order.");
            return;
        }

        //-> Create list of products with quantities
        List<Map<Product, Integer>> productsWithQuantities = new ArrayList<>();
        //-> In a real application, you would get the actual quantities from the cart
        for (Product product : cartProducts) {
            Map<Product, Integer> productQuantity = new HashMap<>();
            productQuantity.put(product, 1); // Assuming quantity 1 for each product
            productsWithQuantities.add(productQuantity);
        }

        System.out.print("Enter shipping address: ");
        String shippingAddress = scanner.nextLine();

        boolean success = orderProcessor.placeOrder(customer, productsWithQuantities, shippingAddress);

        if (success) {
            System.out.println("Order placed successfully!");
        } else {
            System.out.println("Failed to place order.");
        }
    }

    private static void viewCustomerOrders() throws CustomerNotFoundException, OrderNotFoundException {
        System.out.println("\n--- View Customer Orders ---");
        System.out.print("Enter customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine(); //-> Declares newline

        List<Map<Product, Integer>> orders = orderProcessor.getOrdersByCustomer(customerId);

        if (orders.isEmpty()) {
            System.out.println("No orders found for this customer.");
        } else {
            System.out.println("\nCustomer Orders:");
            int orderCount = 1;
            for (Map<Product, Integer> order : orders) {
                System.out.println("\nOrder #" + orderCount++ + ":");
                for (Map.Entry<Product, Integer> entry : order.entrySet()) {
                    Product product = entry.getKey();
                    int quantity = entry.getValue();
                    System.out.println(product.getName() + " - Quantity: " + quantity + " - Price: $" + product.getPrice());
                }
            }
        }
    }
}