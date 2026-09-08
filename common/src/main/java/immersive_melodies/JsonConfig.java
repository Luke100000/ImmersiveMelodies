package immersive_melodies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonConfig {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int version = 0;

    int getVersion() {
        return 1;
    }

    public static File getConfigFile() {
        return new File("./config/" + Common.MOD_ID + ".json");
    }

    public void save() {
        File configFile = getConfigFile();
        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Common.LOGGER.error("Couldn't create config directory {}", parent);
            return;
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            version = getVersion();
            writer.write(toJsonString());
        } catch (IOException e) {
            Common.LOGGER.error(e);
        }
    }

    public String toJsonString() {
        return GSON.toJson(this);
    }

    public static Config loadOrCreate() {
        if (getConfigFile().exists()) {
            try (FileReader reader = new FileReader(getConfigFile())) {
                Config config = GSON.fromJson(reader, Config.class);
                if (config.version != config.getVersion()) {
                    config = new Config();
                }
                config.save();
                return config;
            } catch (Exception e) {
                LOGGER.error("Failed to load Immersive Melodies config! Default config is used for now. Delete the file to reset.");
                LOGGER.error(e);
                return new Config();
            }
        } else {
            Config config = new Config();
            config.save();
            return config;
        }
    }
}
