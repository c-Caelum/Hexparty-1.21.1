package caelum.hexparty;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class HTTPHandler {
    public final HttpClient client = HttpClient.newHttpClient();
    public final HashMap<UUID, Pair<Either<HttpResponse<String>, Throwable>, Integer>> responses = new HashMap<>();
    public static final String copypartyURL = Config.COPYPARTY_URL.get();
    public static final List<? extends String> allowedMethods = Config.ALLOWED_METHODS.get();
    public static final HTTPHandler INSTANCE = new HTTPHandler();

    public void makeAndQueueRequestHex(@NotNull UUID uuid, @NotNull String url, @Nullable String[] headers, @Nullable String method, @Nullable String body, List<? extends Iota> args, int headeridx, int methodidx, int time) {
        try {
            makeAndQueueRequest(uuid, url, headers, method, body, time);
        } catch (InvalidMethodException exception) {
            throw MishapInvalidIota.ofType(args.get(methodidx), methodidx, "valid_method");
        } catch (InvalidHeadersException exception) {
            throw MishapInvalidIota.ofType(args.get(methodidx), methodidx, "valid_headers");
        }
    }

    public void makeAndQueueRequest(@NotNull UUID uuid, @NotNull String url, @Nullable String[] headers, @Nullable String method, @Nullable String body, int time) {
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
        queueRequest(uuid, builder.build(), time);
    }

    void queueRequest(UUID uuid, HttpRequest request, int time) {
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((res, err) -> {
            if (err != null) {
                responses.put(uuid, Pair.of(Either.right(err), time));
            } else if (res != null) {
                responses.put(uuid, Pair.of(Either.left(res), time));
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