package com.avrethem.utils;

import java.util.TreeMap;

public class IssueMap extends TreeMap<String, UtilPair>{

    public IssueMap() {
        super();
    }


    public UtilPair incrementClosed(String key) throws ClassCastException, NullPointerException {
        System.out.println(">>> Close isssue   \t[" + key + "]\t\t@ ");
        UtilPair value = super.containsKey(key) ? super.get(key) : new UtilPair();
        return super.put(key, value.add(0, 1));
    }

    public UtilPair decrementClosed(String key) throws ClassCastException, NullPointerException {
        System.out.println("<<< ReOpen isssue   \t[" + key + "]\t\t@ ");
        UtilPair value = super.containsKey(key) ? super.get(key) : new UtilPair();
        return super.put(key, value.sub(0, 1));
    }

    public UtilPair incrementOpen(String key) throws ClassCastException, NullPointerException {
        System.out.println("## Add open issue \t[" + key + "]");
        UtilPair value = super.containsKey(key) ? super.get(key) : new UtilPair();
        return super.put(key, value.add(1, 0));
    }


}
