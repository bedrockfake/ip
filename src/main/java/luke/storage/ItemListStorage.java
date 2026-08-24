package luke.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;

import luke.tasks.ItemList;

/**
 * Handles loading and saving the task list.
 *
 * <p>The storage package depends on task model classes such as
 * {@link ItemList}, but the task model does not depend on storage.</p>
 */
public class ItemListStorage {
    private static final String FILE_PATH = "./data/itemlist.json";

    /**
     * Creates storage that reads from and writes to Luke's default data file.
     */
    public ItemListStorage() {
    }

    private Path getFilePath(String filePath) throws IOException {
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
     * @throws IOException if the data file cannot be read or created
     */
    public ItemList load() throws IOException {
        Path filePath = getFilePath(FILE_PATH);
        if (Files.size(filePath) == 0) {
            return new ItemList();
        }

        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(filePath)) {
            ItemList itemList = gson.fromJson(reader, ItemList.class);
            return itemList == null ? new ItemList() : itemList;
        }
    }

    /**
     * Saves the task list to disk.
     *
     * @param itemList the task list to save
     * @throws IOException if the data file cannot be written
     */
    public void save(ItemList itemList) throws IOException {
        Path filePath = getFilePath(FILE_PATH);
        Gson gson = new Gson();

        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(itemList, writer);
        }
    }
}
