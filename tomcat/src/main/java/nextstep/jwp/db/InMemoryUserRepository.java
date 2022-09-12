package nextstep.jwp.db;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import nextstep.jwp.model.User;

public class InMemoryUserRepository {

    private static final Map<String, User> database = new ConcurrentHashMap<>();

    static {
        final User user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        database.put(user.getAccount(), user);
    }

    public static void save(User user) {
        validateUserRegistered(user);
        database.put(user.getAccount(), user);
    }

    private static void validateUserRegistered(User user) {
        if (database.containsKey(user.getAccount())) {
            throw new IllegalArgumentException("User account has already registered");
        }
    }

    public static Optional<User> findByAccount(String account) {
        return Optional.ofNullable(database.get(account));
    }

    public static Optional<User> findByAccountAndPassword(String account, String password) {
        Optional<User> foundUser = findByAccount(account);
        if (foundUser.isPresent() && isPasswordMatched(foundUser.get(), password)) {
            return foundUser;
        }
        return Optional.empty();
    }

    private static boolean isPasswordMatched(final User user, final String password) {
        return user.checkPassword(password);
    }

    private InMemoryUserRepository() {
    }
}
