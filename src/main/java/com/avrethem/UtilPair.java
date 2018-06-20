package com.avrethem;

public class UtilPair {
    public int open;
    public int closed;

    public UtilPair() {
        open   = 0;
        closed = 0;
    }

    public UtilPair add(int add_open, int add_closed) {
        open    += add_open;
        closed  += add_closed;
        return this;
    }

    public UtilPair sub(int add_open, int add_closed) {
        open    -= add_open; //(open - add_open <= 0)     ? 0 : open - add_open;
        closed  -= add_closed; //(closed - add_closed <= 0) ? 0 : closed - add_closed;
        return this;
    }

}
