package common.data;

/**
 * Класс, представляющий сложность Лабораторной работы
 */
public enum Difficulty {
    TESTING("Тестовая"),
    VERY_EASY ("Очень легкая"),
    HOPELESS ("Безнадёжная"),
    TERRIBLE ("Ужасающая");

    private String title;

    Difficulty(String title) {
        this.title = title;
    }

    /**
     * Метод, возвращающий название сложности на русском языке
     * @return title
     */
    public String getTitle() {
        if (this == null) {
            return null;
        }
        return title;
    }
    public static Difficulty outOfString(String arg) {
        for (Difficulty x : new Difficulty[]{Difficulty.HOPELESS, Difficulty.TERRIBLE, Difficulty.VERY_EASY}) {
            if (x.toString().equals(arg)) {
                return x;
            }
        }
        return null;
    }
}
