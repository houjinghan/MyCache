package com.github.houbb.cache.core.mytest;

public class Test1 {
    public static void main(String[] args) {
        int a=1,b=1,c=1;
        if(a==1 || isBool(c)){
            System.out.println("aa");
        }
    }

    private static boolean isBool(int c) {
        System.out.println("ccc");
        return true;
    }
}
