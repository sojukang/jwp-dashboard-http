package org.apache.coyote.http11;

import static org.apache.coyote.http11.HttpRequest.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nextstep.jwp.AuthController;
import nextstep.jwp.exception.UncheckedServletException;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {

            final HttpRequest request = new HttpRequest(inputStream);
            final HttpResponse response = getResponse(request);

            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException | URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }

    private HttpResponse getResponse(HttpRequest request) throws URISyntaxException, IOException {
        if (request.getUrl().getPath().endsWith("/login.html")) {
            return AuthController.login(request);
        }

        if (request.getHeaderValue(HTTP_METHOD).equals("POST") && request.getUrl()
            .getPath()
            .endsWith("/register.html")) {
            return AuthController.signUp(request);
        }

        return new HttpResponse(request, StatusCode.OK, getStaticResource(request));
    }

    private String getStaticResource(HttpRequest request) throws IOException {
        final URL requestUrl = request.getUrl();
        if (requestUrl.getPath().equals("/")) {
            return "Hello world!";
        }
        return Files.readString(new File(Objects.requireNonNull(requestUrl).getFile()).toPath());
    }
}
