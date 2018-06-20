package artista.hackathon2;

public class MedObj {

    private String name, desc, image, url;
    private double price;

    public MedObj(String name, String desc, String image, String url, double price) {
        this.name = name;
        this.desc = desc;
        this.image = image;
        this.url = url;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }

    public double getPrice() {
        return price;
    }
}
