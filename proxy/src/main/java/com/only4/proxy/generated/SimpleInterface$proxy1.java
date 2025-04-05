package com.only4.proxy.generated;

import com.only4.proxy.intf.SimpleInterface;

public class SimpleInterface$proxy1 implements SimpleInterface {
    SimpleInterface simpleInterface;

    @Override
    public void func1() {
        System.out.println("before");
        simpleInterface.func1();
        System.out.println("after");
    }

    @Override
    public void func2() {
        System.out.println("before");
        simpleInterface.func2();
        System.out.println("after");
    }

    @Override
    public void func3() {
        System.out.println("before");
        simpleInterface.func3();
        System.out.println("after");
    }
}