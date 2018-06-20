package artista.hackathon2;


public class ProductModal {

    private String prod, link, image, originalText, siteName;
    private int moreCount;
    private Double price;

    public ProductModal(String prod, double price, String link, String image, String originalText, String siteName, int moreCount) {
        this.prod = prod;
        this.price = price;
        this.link = link;
        this.image = image;
        this.originalText = originalText;
        this.siteName = siteName;
        this.moreCount = moreCount;

    }

    public ProductModal(String prod, String link, String siteName, Double price) {
        this.prod = prod;
        this.link = link;
        this.siteName = siteName;
        this.price = price;
    }

    public String getProd() {
        return prod;
    }

    public Double getPrice() {
        return price;
    }

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public int getMoreCount() {
        return moreCount;
    }

    public void setMoreCount(int moreCount) {
        this.moreCount = moreCount;
    }
}
