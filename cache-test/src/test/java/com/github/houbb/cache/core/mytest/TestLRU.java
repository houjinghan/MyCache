package com.github.houbb.cache.core.mytest;

public class TestLRU {
    public static void main(String[] args) {
        LRUCache<String, Integer> cache = new LRUCache<>(10); //cold: 3  hot: 7
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        cache.PrintVaule();
        cache.get("a");
        cache.get("b");
        cache.get("c");
        cache.PrintVaule();
        cache.put("e", 5);
        cache.put("f", 6);
        cache.put("g", 7);
        cache.PrintVaule();
        cache.get("e");
        cache.get("f");
        cache.get("g");
        cache.PrintVaule();
        cache.put("h", 8);
        cache.put("i", 9);
        cache.put("j", 10);
        cache.PrintVaule();
        cache.get("h");
        cache.get("i");
        cache.PrintVaule();
    }
}
