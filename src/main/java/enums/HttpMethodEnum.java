package enums;

public enum HttpMethodEnum {
    GET("GET"),
    POST("POST");

    private String name;

    HttpMethodEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
