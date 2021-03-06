package li.lingfeng.ltweaks.utils;

import android.net.Uri;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import li.lingfeng.ltweaks.MyApplication;

/**
 * Created by smallville on 2017/2/2.
 */

public class IOUtils {

    public static byte[] uri2bytes(Uri uri) {
        byte[] bytes = null;
        InputStream stream = null;
        try {
            stream = MyApplication.instance().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[15360];
            int read;
            while ((read = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            bytes = outputStream.toByteArray();
        } catch (Exception e) {
            Logger.e("uri2bytes error, " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (Exception _) {}
        }
        return bytes;
    }

    public static List<String> readLines(String path) {
        try {
            final File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                Logger.w("File doesn't exist or can't be read, " + path);
                return new ArrayList<>();
            }
            return FileUtils.readLines(file, "utf-8");
        } catch (Exception e) {
            Logger.w("File can't be read, " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
