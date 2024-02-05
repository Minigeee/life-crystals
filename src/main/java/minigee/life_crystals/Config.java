package minigee.life_crystals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public record Config(int healthIncrement, int baseHealth, int maxHealth) {

	/** Config file path */
	public static final String CONFIG_PATH = "config" + File.separator + "life_crystals.json";
	/** Config */
	public static Config DATA = new Config(2, 20, 60);

	/** Gson parser/writer */
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();

	/**
	 * Read or create config data
	 */
	public static void setup() {
		try {
			if (Files.exists(Path.of(CONFIG_PATH))) {
				// Read and parse config
				FileReader reader = new FileReader(CONFIG_PATH);
				DATA = gson.fromJson(reader, Config.class);
				reader.close();
			} else {
				// Create config dir if not exist
				if (Files.notExists(Path.of("config")))
					Files.createDirectory(Path.of("config"));
					
				// Write current config data
                FileWriter writer = new FileWriter(CONFIG_PATH);
                writer.write(gson.toJson(DATA));
                writer.flush();
                writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
