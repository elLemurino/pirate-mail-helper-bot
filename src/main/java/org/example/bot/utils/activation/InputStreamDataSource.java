package org.example.bot.utils.activation;

import jakarta.activation.DataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class InputStreamDataSource implements DataSource {

    /**
     * Default content type documented in {@link DataSource#getContentType()}.
     */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private final String contentType;
    private final InputStream inputStream;
    private final String name;

    /**
     * Constructs a new instance.
     *
     * @param inputStream An input stream.
     * @param contentType A content type.
     */
    public InputStreamDataSource(final InputStream inputStream, final String contentType) {
        this(inputStream, contentType, null);
    }

    /**
     * Constructs a new instance.
     *
     * @param inputStream An input stream.
     * @param contentType A content type.
     * @param name        A name.
     */
    public InputStreamDataSource(final InputStream inputStream, final String contentType, final String name) {
        this.inputStream = inputStream;
        this.contentType = contentType != null ? contentType : DEFAULT_CONTENT_TYPE;
        this.name = name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Always throws {@link UnsupportedOperationException}.
     *
     * @return Always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always throws {@link UnsupportedOperationException}.
     */
    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

}
