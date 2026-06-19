package caelum.hexparty;

import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class HTTPHandler {
    public final HttpClient client = HttpClient.newHttpClient();
    public final HashMap<UUID, Either<HttpResponse<String>, Throwable>> responses = new HashMap<>();
    public static final String copypartyURL = Config.COPYPARTY_URL.get();
    public static final List<? extends String> allowedMethods = Config.ALLOWED_METHODS.get();
    public static final HTTPHandler INSTANCE = new HTTPHandler();

    public void makeAndQueueRequest(@NotNull UUID uuid, @NotNull String url, @Nullable String[] headers, @Nullable String method, @Nullable String body) {
        url = copypartyURL.concat(url);
        method = method == null ? "GET" : method;
        if (!allowedMethods.contains(method)) {
            // my dearest apologies for this one-liner
            throw new InvalidMethodException(allowedMethods, method);
        }
        HttpRequest.BodyPublisher bodyPublisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);

        final HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(copypartyURL.concat(url)));
        builder.method(method, bodyPublisher);
        if (headers != null) {
            if (headers.length % 2 != 0) {
                throw new InvalidHeadersException(headers);
            }
            builder.headers(headers);
        }
        queueRequest(uuid, builder.build());
    }

    void queueRequest(UUID uuid, HttpRequest request) {
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((res, err) -> {
            if (err != null) {
                responses.put(uuid, Either.right(err));
            } else if (res != null) {
                responses.put(uuid, Either.left(res));
            }
        });
    }

    public static class InvalidMethodException extends IllegalArgumentException {
        InvalidMethodException(List<? extends String> allowed, String got) {
            super(String.format("Expected a header in %s, but got %s.", allowed, got));
        }
    }

    public static class InvalidHeadersException extends IllegalArgumentException {
        InvalidHeadersException(String[] headers) {
            super(String.format("Expected a list of headers with an even length, but got %s.", Arrays.toString(headers)));
        }
    }

}