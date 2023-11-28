package com.example.journalapp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AssetUtil {
    public static Drawable getDrawableFromAsset(Context context, String assetName) {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(assetName)) {
            return Drawable.createFromStream(inputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Drawable> getAllDrawablesFromAssets(Context context, String assetsDir) {
        List<Drawable> drawables = new ArrayList<>();
        AssetManager assetManager = context.getAssets();

        try {
            String[] files = assetManager.list(assetsDir);
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".png") || file.endsWith(".jpg") || file.endsWith(".jpeg")) { // Add other drawable formats as needed
                        InputStream inputStream = assetManager.open(assetsDir + "/" + file);
                        Drawable drawable = Drawable.createFromStream(inputStream, null);
                        drawables.add(drawable);
                        inputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return drawables;
    }
}
