package com.example.weldcalc

enum class ProfileType(
    val displayName: String,
    val weightPerMeter: Float,
    val unit: String
) {
    PIPE_ROUND_25("Труба 25x3", 1.70f, "м"),
    PIPE_ROUND_40("Труба 40x3", 2.76f, "м"),
    PIPE_ROUND_50("Труба 50x3", 3.51f, "м"),
    PIPE_SQUARE_20("Квадрат 20x20x2", 1.12f, "м"),
    PIPE_SQUARE_40("Квадрат 40x40x3", 3.45f, "м"),
    PIPE_SQUARE_50("Квадрат 50x50x3", 4.35f, "м"),
    CORNER_50("Уголок 50x50x5", 3.77f, "м"),
    CORNER_75("Уголок 75x75x6", 6.85f, "м"),
    CHANNEL_8("Швеллер 8", 8.04f, "м"),
    CHANNEL_10("Швеллер 10", 10.0f, "м"),
    BEAM_I("Двутавр 10", 10.3f, "м"),
    SHEET_2("Лист 2мм", 15.7f, "м²"),
    SHEET_3("Лист 3мм", 23.55f, "м²"),
    REBAR_12("Арматура 12мм", 0.888f, "м"),
    REBAR_16("Арматура 16мм", 1.58f, "м"),
    CUSTOM("Свой вариант (ввод вручную)", 0f, "м");

    companion object {
        fun getByWeight(perMeter: Float): Float = perMeter
    }
}
