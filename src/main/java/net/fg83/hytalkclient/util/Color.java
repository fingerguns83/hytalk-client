package net.fg83.hytalkclient.util;

public enum Color {
    TEXT("#8A8A8A"),
    BACKGROUND("#19212E"),
    CHANNEL("#353434"),
    RED("#C1292E"),
    GOLD("#EDAE49"),
    BLUE("#00798C"),
    GREEN("#306B34");



    private final String color;

    Color(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
    public Integer getRed() {
        return Integer.parseInt(color.replace("#", "").substring(0, 2), 16);
    }
    public Integer getGreen() {
        return Integer.parseInt(color.replace("#", "").substring(2, 4), 16);
    }
    public Integer getBlue() {
        return Integer.parseInt(color.replace("#", "").substring(4, 6), 16);
    }
}
