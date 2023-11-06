package co.phoenixlab.dds;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public interface DdsReadable {

    /**
     * Reads from the provided inputStream
     * @param inputStream The InputStream to read from
     * @throws InvalidDdsException If the inputStream is not valid DDS data
     * @throws IOException If there was an IO error while reading
     */
    default void read(InputStream inputStream) throws InvalidDdsException, IOException {
        read(new DataInputStream(inputStream));
    }

    /**
     * Reads from the provided inputStream
     * @param inputStream The DataInputStream to read from
     * @throws InvalidDdsException If the inputStream is not valid DDS data
     * @throws IOException If there was an IO error while reading
     */
    void read(DataInputStream inputStream) throws InvalidDdsException, IOException;

    /**
     * Reads from the provided byteChannel
     * @param byteChannel The ReadableByteChannel to read from
     * @throws InvalidDdsException If the byteChannel is not valid DDS data
     * @throws IOException If there was an IO error while reading
     */
    void read(ReadableByteChannel byteChannel) throws InvalidDdsException, IOException;

    /**
     * Reads from the given byteChannel {@code size} bytes.
     * @param byteChannel The ReadableByteChannel to read from
     * @param size The number of bytes to be read (usually the size of the data structure)
     * @throws InvalidDdsException If the byteChannel is not valid DDS data
     * @throws IOException If there was an IO error while reading
     */
    default void read(ReadableByteChannel byteChannel, int size) throws InvalidDdsException, IOException {
        ByteBuffer buf = ByteBuffer.allocate(size);
        int read;
        //noinspection StatementWithEmptyBody
        while ((read = byteChannel.read(buf)) > 0);
        if (read < 0) {
            throw new EOFException();
        }
        buf.flip();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        read(buf);
    }

    /**
     * Reads from the given byte array
     * @param data The byte array to read from
     * @throws InvalidDdsException If the byte array is not valid DDS data
     */
    default void read(byte[] data) throws InvalidDdsException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            read(byteArrayInputStream);
        } catch (IOException e) {
            //  Cannot happen
        }
    }

    /**
     * Reads from the given buffer
     * @param buf The ByteBuffer to read from
     * @throws InvalidDdsException If the buffer is not valid DDS data
     */
    void read(ByteBuffer buf) throws InvalidDdsException;

    /**
     * Validates the data that's been read and throws an exception if it's not valid
     * @throws InvalidDdsException If the data that's been read is invalid
     */
    void validate() throws InvalidDdsException;
}
