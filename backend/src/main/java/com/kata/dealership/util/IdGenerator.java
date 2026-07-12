package com.kata.dealership.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static String generate(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
