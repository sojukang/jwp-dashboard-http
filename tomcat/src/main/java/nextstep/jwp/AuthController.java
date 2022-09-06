package nextstep.jwp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.coyote.http11.HttpCookie;
import org.apache.coyote.http11.HttpRequest;
import org.apache.coyote.http11.HttpResponse;
import org.apache.coyote.http11.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.model.User;

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final SessionManager sessionManager = new SessionManager();

    private AuthController() {
    }

    public static HttpResponse login(HttpRequest request) {
        if (isLogin(request)) {
            return HttpResponse.redirect(request, "/index.html");
        }

        if (!request.hasQuery()) {
            return new HttpResponse(request, StatusCode.OK, getStaticResource(request.getUrl()));
        }

        Session session = new Session(new HttpCookie().getCookieValue("JSESSIONID"));

        final String account = request.getQueryValue("account");
        final String password = request.getQueryValue("password");

        Optional<User> foundUser = InMemoryUserRepository.findByAccountAndPassword(account, password);
        if (foundUser.isEmpty()) {
            return HttpResponse.redirect(request, "/401.html");
        }

        User user = foundUser.get();
        session.setAttribute("user", user);
        sessionManager.add(session);
        log.info("로그인 성공! 아이디: {}", user.getAccount());

        HttpResponse response = HttpResponse.redirect(request, "/index.html");
        response.setCookie(HttpCookie.fromJSESSIONID(session.getId()));
        return response;
    }

    private static boolean isLogin(HttpRequest request) {
        if (!request.hasSession()) {
            return false;
        }

        Session session = request.getSession();
        if (!sessionManager.hasSession(session.getId())) {
            return false;
        }

        User user = getUser(sessionManager.findSession(session.getId()));
        return InMemoryUserRepository.findByAccount(user.getAccount())
            .isPresent();
    }

    private static User getUser(Session session) {
        return (User)session.getAttribute("user");
    }

    public static HttpResponse signUp(HttpRequest request) {
        final String account = request.getQueryValue("account");
        final String password = request.getQueryValue("password");
        final String email = request.getQueryValue("email");

        User user = new User(account, password, email);
        InMemoryUserRepository.save(user);
        log.info("회원가입 성공! 아이디: {}", user.getAccount());
        return HttpResponse.redirect(request, "/index.html");
    }

    private static String getStaticResource(URL url) {
        try {
            return new String(Files.readAllBytes(new File(Objects.requireNonNull(url)
                .getFile())
                .toPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException("No such resource");
        }
    }
}
