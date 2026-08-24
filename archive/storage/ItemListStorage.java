package luke.storage;

import luke.tasks.ItemList;

/**
 * Handles loading and saving the task list.
 *
 * <p>The storage package depends on task model classes such as
 * {@link ItemList}, but the task model does not depend on storage.</p>
 */
public class ItemListStorage {
    /**
     * Creates a storage helper.
     */
    public ItemListStorage() {
    }

    /**
     * Loads the saved task list.
     *
     * @return an empty task list until file loading is implemented
     */
    public ItemList load() {
        return new ItemList();
    }
}
