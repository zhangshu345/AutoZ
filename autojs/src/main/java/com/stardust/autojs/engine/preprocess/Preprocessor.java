package com.stardust.autojs.engine.preprocess;

import java.io.IOException;
import java.io.Reader;

/**

 */

public interface Preprocessor {

    Reader preprocess(Reader reader) throws IOException;
}
