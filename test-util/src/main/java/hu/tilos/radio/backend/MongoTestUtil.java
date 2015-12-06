package hu.tilos.radio.backend;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MongoTestUtil {


    public static String loadTo(FongoRule fongoRule, String collection, String resourceName, String... references) {

        String json = loadFrom(resourceName, references);
        DBObject parsed = (DBObject) JSON.parse(json);
        fongoRule.getDB().getCollection(collection).insert(parsed);
        return ((ObjectId) parsed.get("_id")).toHexString();
    }

    public static String loadFrom(String resourceName, String... references) {
        InputStream resourceAsStream = MongoTestUtil.class.getResourceAsStream("/testdata/" + resourceName);
        if (resourceAsStream == null) {
            throw new RuntimeException("Resource is missing: " + resourceName);
        }
        String json = new Scanner(resourceAsStream).useDelimiter("//Z").next();
        for (int i = 1; i < references.length + 1; i++) {
            json = json.replaceAll("<REF" + i + ">", references[i - 1]);
        }
        return json;
    }

    public static FongoRule createRule() {
        try {
            return new FongoRule("unit", true, new MongoClient("localhost"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
