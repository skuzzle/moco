package de.uni.bremen.monty.moco.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MontyCodeString implements MontyResource {

    private static final MontyResource[] EMPTY = new MontyResource[0];

    private final String resourceName;
    private final String code;

    public MontyCodeString(String resourceName, String code) {
        this.resourceName = resourceName;
        this.code = code;
    }

    @Override
    public MontyResource[] listSubPackages() {
        return EMPTY;
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return new ByteArrayInputStream(this.code.getBytes("UTF-8"));
    }

    @Override
    public String getName() {
        return this.resourceName;
    }

    @Override
    public MontyResource[] listSubModules() throws IOException {
        return EMPTY;
    }

}
