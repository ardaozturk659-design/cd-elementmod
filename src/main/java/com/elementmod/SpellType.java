package com.elementmod;

/** Her elementin 12 büyüsü bu sırayla gelir. cooldown tick cinsinden (20 tick = 1 sn). */
public enum SpellType {
    BOLT(10),       // 1  ok/mermi
    LIGHTNING(40),  // 2  elementin kendi renkli yıldırımı
    STORM(100),     // 3  yıldırım fırtınası
    NOVA(80),       // 4  halka patlaması
    BEAM(40),       // 5  ışın
    SHIELD(200),    // 6  kalkan
    HEAL(160),      // 7  şifa
    HASTE(200),     // 8  hız
    CURSE(80),      // 9  lanet
    LEAP(60),       // 10 sıçrayış
    BARRAGE(140),   // 11 yağmur
    ULTIMATE(600);  // 12 öfke (en güçlü)

    public final int cooldown;
    SpellType(int cooldown) { this.cooldown = cooldown; }
}
