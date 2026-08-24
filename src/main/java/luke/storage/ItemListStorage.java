package luke.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import luke.exceptions.StorageException;
import luke.tasks.ItemList;

/**
 * Handles loading and saving the task list.
 *
 * <p>The storage package depends on task model classes such as
 * {@link ItemList}, but the task model does not depend on storage.</p>
 */
public final class ItemListStorage {
    private static final String STORAGE_PATH_PROPERTY = "luke.storage.path";
    private static final String DEFAULT_FILE_PATH = "./data/itemlist.json";

    private ItemListStorage() {
    }

    /**
     * Returns the storage path to use. Tests can override the default path so
     * they do not read from or write to the user's real task file.
     *
     * @return configured storage path, or the default path if none is configured
     */
    private static String getStoragePath() {
        return System.getProperty(STORAGE_PATH_PROPERTY, DEFAULT_FILE_PATH);
    }

    private static Path getFilePath(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    /**
     * Loads the task list from disk.
     *
     * @return the saved task list, or an empty list if no saved data exists
     * @throws StorageException if the data file cannot be read or created
     */
    public static ItemList load() throws StorageException {
        try {
            Path filePath = getFilePath(getStoragePath());
            if (Files.size(filePath) == 0) {
                return new ItemList();
            }

            Gson gson = new Gson();
            try (Reader reader = Files.newBufferedReader(filePath)) {
                ItemList itemList = gson.fromJson(reader, ItemList.class);
                if (itemList == null) {
                    return new ItemList();
                }
                itemList.validateLoadedItems();
                return itemList;
            }
        } catch (IOException | JsonParseException | IllegalStateException e) {
            throw StorageException.loadFailed(e);
        }
    }

    /**
     * Saves the task list to disk.
     *
     * @param itemList the task list to save
     * @throws StorageException if the data file cannot be written
     */
    public static void save(ItemList itemList) throws StorageException {
        try {
            Path filePath = getFilePath(getStoragePath());
            Gson gson = new Gson();

            try (Writer writer = Files.newBufferedWriter(filePath)) {
                gson.toJson(itemList, writer);
            }
        } catch (IOException | JsonParseException e) {
            throw StorageException.saveFailed(e);
        }
    }
}
