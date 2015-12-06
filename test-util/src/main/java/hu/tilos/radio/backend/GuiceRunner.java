package hu.tilos.radio.backend;

import com.github.fakemongo.junit.FongoRule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import hu.tilos.radio.backend.spark.GuiceConfigurationListener;
import org.dozer.DozerBeanMapper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.validation.Validator;
import java.net.UnknownHostException;

public class GuiceRunner implements TestRule {

    public GuiceRunner(Object obj) {

        FongoCreator creator = new FongoCreator();
        creator.createDB();
        creator.init();
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB("tilos");
            Injector i = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(FongoRule.class).toInstance(creator.createRule());
                    bind(DB.class).toInstance(db);
                    bind(DozerBeanMapper.class).toProvider(DozerFactory.class).asEagerSingleton();
                    bind(Validator.class).toProvider(hu.tilos.radio.backend.ValidatorProducer.class);
                    bindListener(Matchers.any(), new GuiceConfigurationListener());
                }


            });

            i.injectMembers(obj);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return base;
    }

}
