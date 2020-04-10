package ua.cn.stu.randomgallery.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.cn.stu.randomgallery.GalleryStorage;

public class SimpleGalleryStorage implements GalleryStorage {
    private static final String GALLERY_DATA_PREFERENCES_NAME = "DATA";
    private static final String GALLERY_TIMESTAMP_PREFERENCES_NAME = "TIMESTAMP";
    private SharedPreferences dataPreferences;
    private SharedPreferences timestampPreferences;
    private Context context;

    public SimpleGalleryStorage(Context context) {
        dataPreferences = context.getSharedPreferences(GALLERY_DATA_PREFERENCES_NAME, Context.MODE_PRIVATE);
        timestampPreferences = context.getSharedPreferences(GALLERY_TIMESTAMP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.context = context;
    }
    private File getFileByIdentifier(String id) {
        File dir = context.getFilesDir();
        return new File(dir,id);
    }
    @Override
    public InputStream read(String identifier) throws IOException {

        return new FileInputStream(getFileByIdentifier(identifier));
    }

    @Override
    public OutputStream write(String identifier, String name) throws IOException {
        dataPreferences.edit().putString(identifier, name).apply();;
        return new FileOutputStream(getFileByIdentifier(identifier));
    }

    @Override
    public boolean isExists(String identifier) {
        return getFileByIdentifier(identifier).exists();
    }

    @Override
    public String getName(String identifier) {
        return dataPreferences.getString(identifier,"");
    }

    @Override
    public void delete(String identifier) {
        dataPreferences.edit().remove(identifier).apply();
      getFileByIdentifier(identifier).delete();
    }

    @Override
    public void rename(String oldIdentifier, String newIdentifier) {
       File oldFile = getFileByIdentifier(oldIdentifier);
        File newFile = getFileByIdentifier(newIdentifier);
        oldFile.renameTo(newFile);
        String nameValue = getName(oldIdentifier);
        dataPreferences.edit().remove(oldIdentifier).putString(newIdentifier, nameValue).apply();
    }

    @Override
    public List<String> getAll() {
        Set<String> allIdentifiers = dataPreferences.getAll().keySet();

        return new ArrayList<>(allIdentifiers);
    }

    @Override
    public long getTimestamp() {
        return timestampPreferences.getLong(GALLERY_TIMESTAMP_PREFERENCES_NAME, 0);
    }

    @Override
    public void setTimestamp(long timestamp) {
        timestampPreferences.edit().putLong(GALLERY_TIMESTAMP_PREFERENCES_NAME,timestamp).apply();;

    }
}
