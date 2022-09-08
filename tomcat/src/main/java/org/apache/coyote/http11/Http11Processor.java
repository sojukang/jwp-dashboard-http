package org.apache.coyote.http11;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import org.apache.catalina.handler.Controller;
import org.apache.catalina.handler.HandlerMapping;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nextstep.jwp.exception.UncheckedServletException;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;
    private final HandlerMapping handlerMapping;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
        this.handlerMapping = new HandlerMapping();
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

    private HttpResponse getResponse(HttpRequest request) throws IOException {
        Controller controller = handlerMapping.getController(request);
        if (isHandlerFound(controller)) {
            return controller.service(request);
        }

        return new HttpResponse.Builder(request)
            .ok()
            .messageBody(getStaticResource(request)).build();
    }

    private boolean isHandlerFound(Controller controller) {
        return controller != null;
    }

    private String getStaticResource(HttpRequest request) throws IOException {
        final URL requestUrl = request.getUrl();
        if (requestUrl.getPath().equals("/")) {
            return "Hello world!";
        }
        return Files.readString(new File(Objects.requireNonNull(requestUrl).getFile()).toPath());
    }
}
