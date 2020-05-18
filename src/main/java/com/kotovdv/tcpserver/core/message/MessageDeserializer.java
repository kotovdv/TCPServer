package com.kotovdv.tcpserver.core.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;

public interface MessageDeserializer<T> {

    /**
     * Attempts to read next N bytes in attempt to transform them to instance of type T.
     *
     * @param stream InputStream with data.
     * @return Optional with instance of type T in it, as long as stream contains proper data and is still open.
     * If there is no data, but stream is still open call to this method will result in caller's thread being blocked until new data arrives.
     * If there is no more data and stream is closed - Optional.empty is returned.
     * @throws IOException If failed to transform next N bytes to instance of type T.
     */
    Optional<T> readNext(DataInputStream stream) throws IOException;
}
