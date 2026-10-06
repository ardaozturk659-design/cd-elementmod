package com.elementmod;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 6 element x 12 büyü = 72 büyü. Sıra SpellType sırasıyla aynı. */
public class SpellRegistry {
    private static final Map<Element, List<Spell>> MAP = new EnumMap<>(Element.class);

    static {
        add(Element.WATER, "Su Oku", "Mavi Yıldırım", "Mavi Fırtına", "Dalga Patlaması", "Su Işını", "Buz Kalkanı",
                "Şifalı Pınar", "Akıntı Hızı", "Boğulma Laneti", "Dalga Sıçrayışı", "Yağmur Okları", "Okyanus Öfkesi");
        add(Element.FIRE, "Ateş Oku", "Kızıl Yıldırım", "Alev Fırtınası", "Ateş Halkası", "Alev Işını", "Lav Kalkanı",
                "Anka Şifası", "Alev Hızı", "Yanma Laneti", "Roket Sıçrayışı", "Ateş Yağmuru", "Cehennem Öfkesi");
        add(Element.EARTH, "Taş Oku", "Yeşil Yıldırım", "Toprak Fırtınası", "Deprem Halkası", "Kristal Işını", "Taş Kalkan",
                "Doğa Şifası", "Orman Hızı", "Kök Laneti", "Dağ Sıçrayışı", "Kaya Yağmuru", "Titan Öfkesi");
        add(Element.ELECTRIC, "Kıvılcım Oku", "Elektrik Yıldırımı", "Elektrik Fırtınası", "Şok Halkası", "Plazma Işını", "Statik Kalkan",
                "Enerji Şifası", "Şimşek Hızı", "Felç Laneti", "Şimşek Sıçrayışı", "Kıvılcım Yağmuru", "Tesla Öfkesi");
        add(Element.LIGHT, "Işık Oku", "Sarı Yıldırım", "Güneş Fırtınası", "Parlama Halkası", "Güneş Işını", "Kutsal Kalkan",
                "Kutsal Şifa", "Şafak Hızı", "Işık Hükmü", "Şafak Sıçrayışı", "Yıldız Yağmuru", "Güneş Öfkesi");
        add(Element.DARK, "Gölge Oku", "Siyah Yıldırım", "Karanlık Fırtına", "Karanlık Halkası", "Boşluk Işını", "Gölge Kalkanı",
                "Ruh Emme", "Gölge Hızı", "Ölüm Laneti", "Gölge Sıçrayışı", "Kara Yağmur", "Boşluk Öfkesi");
    }

    private static void add(Element e, String... names) {
        List<Spell> list = new ArrayList<>();
        SpellType[] types = SpellType.values();
        for (int i = 0; i < names.length; i++) list.add(new Spell(e, types[i], names[i]));
        MAP.put(e, list);
    }

    public static List<Spell> get(Element e) { return MAP.get(e); }
}
