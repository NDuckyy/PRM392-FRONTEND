package com.example.prm392_frontend;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Product implements Parcelable {
    private int id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private List<String> imageUrls;
    private String category;
    private double rating;
    private String brand;
    private int popularity;
    private String specifications;

    public Product(int id, String name, String description, double price, String imageUrl,
                   List<String> imageUrls, String category, double rating, String brand,
                   int popularity, String specifications) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.category = category;
        this.rating = rating;
        this.brand = brand;
        this.popularity = popularity;
        this.specifications = specifications;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        imageUrl = in.readString();
        imageUrls = in.createStringArrayList();
        category = in.readString();
        rating = in.readDouble();
        brand = in.readString();
        popularity = in.readInt();
        specifications = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public double getRating() {
        return rating;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getBrand() {
        return brand;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getSpecifications() {
        return specifications;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(imageUrl);
        dest.writeStringList(imageUrls);
        dest.writeString(category);
        dest.writeDouble(rating);
        dest.writeString(brand);
        dest.writeInt(popularity);
        dest.writeString(specifications);
    }
}
