package com.supermartijn642.configlib.toml;

import java.io.IOException;

/**
 * Created 09/04/2022 by SuperMartijn642
 */
public class MalformedTomlException extends IOException {

    public MalformedTomlException(String msg) {
        super(msg);
    }
}
