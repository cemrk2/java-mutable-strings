package me.cemrk;

import java.util.Locale;

public final class StringEx extends String {

    public void m_set(String s2) {
        this.value = s2.value;
        this.hash = s2.hash;
        s2.value = new char[0];
    }

    public void m_append(String s2) {
        m_set(this + s2);
    }

    public void m_replace(char c1, char c2) {
        m_set(this.replace(c1, c2));
    }

    public void m_replace(CharSequence sq1, CharSequence sq2) {
        m_set(this.replace(sq1, sq2));
    }

    public void m_concat(String s) {
        m_set(this.concat(s));
    }

    public void m_toUpperCase() {
        m_set(this.toUpperCase());
    }

    public void m_toUpperCase(Locale locale) {
        m_set(this.toUpperCase(locale));
    }

    public void m_toLowerCase() {
        m_set(this.toLowerCase());
    }

    public void m_toLowerCase(Locale locale) {
        m_set(this.toLowerCase(locale));
    }

    public void m_replaceFirst(String s1, String s2) {
        m_set(this.replaceFirst(s1, s2));
    }

    public void m_replaceAll(String s1, String s2) {
        m_set(this.replaceAll(s1, s2));
    }

}
