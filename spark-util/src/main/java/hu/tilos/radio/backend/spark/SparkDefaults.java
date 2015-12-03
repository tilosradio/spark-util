package hu.tilos.radio.backend.spark;

import com.google.gson.*;
import com.google.inject.Injector;
import hu.radio.tilos.model.Role;
import hu.tilos.radio.backend.data.error.AccessDeniedException;
import hu.tilos.radio.backend.data.error.NotFoundException;
import hu.tilos.radio.backend.data.error.ValidationException;
import hu.tilos.radio.backend.data.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Route;

import java.lang.reflect.Type;
import java.util.Date;

import static spark.Spark.*;

public class SparkDefaults {


    Injector injector;

    private Gson gson;

    private final JsonTransformer jsonTransformer;

    public SparkDefaults(int port, Injector injector) {
        port(port);
        gson = createGsonTransformer();
        init(gson);

        jsonTransformer = new JsonTransformer(gson);
        this.injector = injector;
    }

    private static final Logger LOG = LoggerFactory.getLogger(SparkDefaults.class);

    private Gson createGsonTransformer() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date src, Type type, JsonSerializationContext jsonSerializationContext) {
                        return src == null ? null : new JsonPrimitive(src.getTime());
                    }
                })
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                        return json == null ? null : new Date(json.getAsLong());
                    }
                })
                .create();
        return gson;
    }


    public void init(Gson gson) {


        before((request, response) -> {
            LOG.info(request.uri());
        });
        before((request, response) -> response.type("application/json"));

        exception(NotFoundException.class, (e, request, response) -> {
            response.status(404);
            response.body(gson.toJson(new ErrorResponse(e.getMessage())));
        });

        exception(AccessDeniedException.class, (e, request, response) -> {
            response.status(403);
            response.body(gson.toJson(new ErrorResponse("Hibás user név vagy jelszó")));
        });

        exception(NullPointerException.class, (e, request, response) -> {
            LOG.error("Error", e);
            response.status(500);
            response.body(gson.toJson(new ErrorResponse("Alkalmazás hiba történt, kérlek írj a webmester@tilos.hu címre.")));
        });

        exception(IllegalArgumentException.class, (e, request, response) -> {
            LOG.error("Illegal argument", e);
            response.status(400);
            response.body(gson.toJson(new ErrorResponse(e.getMessage())));
        });

        exception(ValidationException.class, (e, request, response) -> {
            response.status(400);
            response.body(gson.toJson(new ErrorResponse(e.getMessage())));
        });

    }

    public JsonTransformer getJsonTransformer() {
        return jsonTransformer;
    }

    public boolean booleanParam(Request req, String param) {
        String full = req.queryParams(param);
        if (full != null) {
            return Boolean.parseBoolean(full);
        } else {
            return false;
        }
    }


    public Integer intParam(Request req, String name) {
        if (req.queryParams(name) == null) {
            return null;
        } else {
            return Integer.parseInt(req.queryParams(name));
        }
    }

    public Long longParam(Request req, String name) {
        if (req.queryParams(name) == null) {
            return null;
        } else {
            return Long.parseLong(req.queryParams(name));
        }
    }

    public Route authorized(Role role, AuthorizedRoute authorizedRoute) {
        Authorized authorized = new Authorized(role, authorizedRoute);
        injector.injectMembers(authorized);
        return authorized;
    }

    public Route authorized(String permission, AuthorizedRoute authorizedRoute) {
        Authorized authorized = new Authorized(permission, authorizedRoute);
        injector.injectMembers(authorized);
        return authorized;
    }
}
