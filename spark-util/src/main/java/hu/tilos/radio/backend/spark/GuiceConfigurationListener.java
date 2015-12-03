package hu.tilos.radio.backend.spark;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import hu.tilos.radio.backend.Configuration;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class GuiceConfigurationListener implements TypeListener, ProvisionListener {


    Config config = ConfigFactory.load();

    public GuiceConfigurationListener() {
        int defaultPort = config.getInt("port.default");
        Set<Map.Entry<String, ConfigValue>> configs = config.getConfig("port").entrySet();
        for (Map.Entry<String, ConfigValue> cv : configs) {
            if (cv.getKey().endsWith(".offset")) {
                config = config.withValue("port." + cv.getKey().replace(".offset", ""), ConfigValueFactory.fromAnyRef(defaultPort + ((Integer) cv.getValue().unwrapped())));
            }
        }
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provisionInvocation) {

    }

    @Override
    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {

        Class<?> clazz = typeLiteral.getRawType();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Configuration.class)) {
                    typeEncounter.register(new ConfigurationInjector<I>(field, config));
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
