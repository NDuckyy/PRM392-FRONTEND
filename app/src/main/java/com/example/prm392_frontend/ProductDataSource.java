package com.example.prm392_frontend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductDataSource {

    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();

        products.add(new Product(
            1,
            "Wireless Headphones",
            "Premium noise-cancelling wireless headphones with 30-hour battery life. Features advanced active noise cancellation, premium sound quality, and comfortable over-ear design. Perfect for music lovers and professionals.",
            299.99,
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
            Arrays.asList(
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944",
                "https://images.unsplash.com/photo-1545127398-14699f92334b"
            ),
            "Electronics",
            4.5,
            "Sony",
            1250,
            "• Driver: 40mm\n• Frequency: 20Hz-20kHz\n• Battery: 30 hours\n• Bluetooth: 5.0\n• Weight: 250g"
        ));

        products.add(new Product(
            2,
            "Smart Watch Pro",
            "Advanced fitness tracking smartwatch with heart rate monitor, GPS, and waterproof design. Track your workouts, monitor your health, and stay connected with smartphone notifications.",
            399.99,
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30",
            Arrays.asList("https://images.unsplash.com/photo-1523275335684-37898b6baf30", "https://images.unsplash.com/photo-1579586337278-3befd40fd17a"),
            "Electronics",
            4.7,
            "Apple",
            2100,
            "• Display: 1.9\" AMOLED\n• Battery: 48 hours\n• Water Resistance: 50m\n• GPS: Built-in\n• Sensors: Heart Rate, SpO2, ECG"
        ));

        products.add(new Product(
            3,
            "Running Shoes",
            "Lightweight running shoes with superior cushioning and breathable mesh upper. Engineered for performance with responsive foam and durable rubber outsole. Available in multiple colors.",
            129.99,
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
            Arrays.asList("https://images.unsplash.com/photo-1542291026-7eec264c27ff", "https://images.unsplash.com/photo-1460353581641-37baddab0fa2"),
            "Sports",
            4.3,
            "Nike",
            890,
            "• Material: Mesh upper\n• Sole: Rubber\n• Weight: 280g\n• Cushioning: React foam\n• Sizes: 6-13"
        ));

        products.add(new Product(
            4,
            "Leather Backpack",
            "Handcrafted genuine leather backpack with laptop compartment. Features premium full-grain leather, padded straps, and multiple organizational pockets. Perfect for work and travel.",
            179.99,
            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62",
            Arrays.asList("https://images.unsplash.com/photo-1553062407-98eeb64c6a62", "https://images.unsplash.com/photo-1548036328-c9fa89d128fa"),
            "Fashion",
            4.6,
            "Fossil",
            650,
            "• Material: Full-grain leather\n• Laptop: Up to 15 inches\n• Dimensions: 40x30x15cm\n• Weight: 1.2kg\n• Pockets: 5 compartments"
        ));

        products.add(new Product(
            5,
            "Coffee Maker",
            "Programmable drip coffee maker with thermal carafe. Brew up to 12 cups of perfect coffee with adjustable strength settings and auto-brew timer. Stainless steel construction.",
            89.99,
            "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6",
            Arrays.asList("https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6"),
            "Home",
            4.4,
            "Cuisinart",
            430,
            "• Capacity: 12 cups\n• Carafe: Thermal stainless steel\n• Timer: 24-hour programmable\n• Power: 1000W\n• Auto shut-off"
        ));

        products.add(new Product(
            6,
            "Yoga Mat",
            "Premium non-slip yoga mat with extra cushioning. Made from eco-friendly materials with superior grip and support. Includes carrying strap. Ideal for yoga, pilates, and floor exercises.",
            49.99,
            "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f",
            Arrays.asList("https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f"),
            "Sports",
            4.8,
            "Lululemon",
            1520,
            "• Thickness: 5mm\n• Material: TPE eco-friendly\n• Dimensions: 183x61cm\n• Weight: 1kg\n• Non-slip texture"
        ));

        products.add(new Product(
            7,
            "Desk Lamp",
            "LED desk lamp with adjustable brightness and color temperature. Features touch controls, USB charging port, and flexible gooseneck. Energy-efficient and eye-friendly lighting.",
            59.99,
            "https://images.unsplash.com/photo-1507473885765-e6ed057f782c",
            Arrays.asList("https://images.unsplash.com/photo-1507473885765-e6ed057f782c"),
            "Home",
            4.2,
            "TaoTronics",
            320,
            "• LED: 12W\n• Color Temp: 3000K-6000K\n• Brightness: 5 levels\n• USB Port: 5V/1A\n• Lifespan: 50,000 hours"
        ));

        products.add(new Product(
            8,
            "Sunglasses",
            "Polarized sunglasses with UV400 protection and lightweight frame. Classic aviator style with premium polarized lenses that reduce glare. Comes with protective case.",
            79.99,
            "https://images.unsplash.com/photo-1572635196237-14b3f281503f",
            Arrays.asList("https://images.unsplash.com/photo-1572635196237-14b3f281503f", "https://images.unsplash.com/photo-1511499767150-a48a237f0083"),
            "Fashion",
            4.5,
            "Ray-Ban",
            780,
            "• Lens: Polarized UV400\n• Frame: Metal alloy\n• Style: Aviator\n• Weight: 30g\n• Includes: Hard case, cloth"
        ));

        return products;
    }
}
