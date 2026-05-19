package vn.edu.networkprogramming.clientweb.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MultipartBodyBuilder {

    private final String boundary;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private MultipartBodyBuilder(String boundary) {
        this.boundary = boundary;
    }

    public static MultipartBodyBuilder create(String boundary) {
        return new MultipartBodyBuilder(boundary);
    }

    public MultipartBodyBuilder addField(String name, String value) throws IOException {
        write("--" + boundary + "\r\n");
        write("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        write(value + "\r\n");
        return this;
    }

    public MultipartBodyBuilder addFile(String name, String filename, byte[] content) throws IOException {
        write("--" + boundary + "\r\n");
        write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n");
        write("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n");
        outputStream.write(content);
        write("\r\n");
        return this;
    }

    public byte[] build() throws IOException {
        write("--" + boundary + "--\r\n");
        return outputStream.toByteArray();
    }

    private void write(String value) throws IOException {
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
    }
}
