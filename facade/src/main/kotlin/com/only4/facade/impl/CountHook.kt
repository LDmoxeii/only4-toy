package com.only4.facade.impl

import com.only4.facade.spi.ResponseSPI
import java.util.concurrent.atomic.AtomicInteger

class CountHook: ResponseSPI {
    private var count = AtomicInteger(0)

    override fun before() {
        println(count.incrementAndGet())
    }
}
