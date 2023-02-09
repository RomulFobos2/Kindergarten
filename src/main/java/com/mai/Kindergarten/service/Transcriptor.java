package com.mai.Kindergarten.service;

import com.ibm.icu.text.Transliterator;
import org.springframework.stereotype.Service;

@Service
public class Transcriptor {
    public static final String CYRILLIC_TO_LATIN = "Cyrillic-Latin";

    public static String transliterate(String s) {
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        String result = toLatinTrans.transliterate(s);
        return result;
    }
}
