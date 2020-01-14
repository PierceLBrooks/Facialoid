package com.piercelbrooks.common;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class Utilities {
    private static final String TAG = "PLB-Utilities";

    public static boolean enable(@Nullable View view) {
        if (view == null) {
            return false;
        }
        view.setClickable(true);
        view.setVisibility(View.VISIBLE);
        return true;
    }

    public static boolean disable(@Nullable View view) {
        if (view == null) {
            return false;
        }
        view.setClickable(false);
        view.setVisibility(View.INVISIBLE);
        return true;
    }

    public static boolean bringToFront(@Nullable View view) {
        if (view == null) {
            return false;
        }
        ViewParent parent = view.getParent();
        if (parent == null) {
            return false;
        }
        parent.bringChildToFront(view);
        parent.requestLayout();
        return true;
    }

    public static boolean resetParent(@Nullable View view, @Nullable View parent) {
        return resetParent(view, parent, false);
    }

    public static boolean resetParent(@Nullable View view, @Nullable View parent, boolean force) {
        if (view == null) {
            return false;
        }
        ViewParent temp = view.getParent();
        if (temp == null) {
            if (parent != null) {
                ((ViewGroup) parent).addView(view);
            }
        } else {
            if (force) {
                ((ViewGroup) temp).removeView(view);
                if (parent != null) {
                    ((ViewGroup) parent).addView(view);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static void throwInstanceException(@NonNull String tag, int limit) throws InstanceException {
        throw new InstanceException(tag, limit);
    }

    public static void throwSingletonException(@NonNull String tag) throws SingletonException {
        throw new SingletonException(tag);
    }

    public static void throwUnimplementedException(@NonNull String tag) throws UnimplementedException {
        throw new UnimplementedException(tag);
    }

    public static String getIdentifier(@Nullable Object object) {
        if (object == null) {
            return "<NULL>";
        }
        return "<\"" + object.toString() + "\" (#" + Integer.toHexString(object.hashCode()) + ")>";
    }

    public static String getHex(int number) {
        return Integer.toHexString(number);
    }

    public static boolean closeKeyboard(Activity activity) {
        if (activity == null) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        View focus = activity.getCurrentFocus();
        if (focus == null) {
            return false;
        }
        imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        return true;

    }

    public static boolean openKeyboard(Activity activity) {
        if (activity == null) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        imm.toggleSoftInput(0, 0);
        return true;
    }

    public static boolean openKeyboard(Activity activity, View view) {
        if ((activity == null) || (view == null)) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        imm.showSoftInput(view, 0);
        return true;
    }

    public static void add(List<String> list, List<String> other) {
        add(list, other, null);
    }

    public static void add(List<String> list, List<String> other, String prefix) {
        if ((list != null) && (other != null)) {
            if (prefix != null) {
                for (int i = 0; i != other.size(); ++i) {
                    list.add(prefix+other.get(i));
                }
            } else {
                for (int i = 0; i != other.size(); ++i) {
                    list.add(other.get(i));
                }
            }
        }
    }

    public static int count(String subject, char object) {
        if (subject == null) {
            return 0;
        }
        int tally = 0;
        for (int i = 0; i != subject.length(); ++i) {
            if (subject.charAt(i) == object) {
                ++tally;
            }
        }
        return tally;
    }

    public static boolean read(String path, List<String> lines) {
        if ((path == null) || (lines == null)) {
            return false;
        }
        boolean success = true;
        FileReader reader = null;
        BufferedReader buffer = null;
        Log.i(TAG, "Reading ("+path+")...");
        try {
            String line;
            File file = new File(path);
            reader = new FileReader(file);
            buffer = new BufferedReader(reader);
            while ((line = buffer.readLine()) != null) {
                lines.add(line);
                Log.d(TAG, "Read: \""+line+"\"");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            success = false;
        } finally {
            try {
                if (buffer != null) {
                    buffer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                success = false;
            }
        }
        if (success)
        {
            Log.i(TAG, "Read successfully ("+path+")!");
        }
        else
        {
            Log.e(TAG, "Read unsuccessfully ("+path+")!");
        }
        return success;
    }

    public static boolean delete(String path) {
        if (path == null) {
            return false;
        }
        boolean success = true;
        Log.i(TAG, "Deleting ("+path+")...");
        try {
            File file = new File(path);
            if (file.exists()) {
                success = file.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            success = false;
        }
        if (success)
        {
            Log.i(TAG, "Deleted successfully ("+path+")!");
        }
        else
        {
            Log.e(TAG, "Deleted unsuccessfully ("+path+")!");
        }
        return success;
    }

    public static boolean write(String path, List<String> lines) {
        if ((path == null) || (lines == null)) {
            return false;
        }
        boolean success = true;
        FileWriter writer = null;
        BufferedWriter buffer = null;
        String[] split = path.split(File.separator);
        path = ""+File.separatorChar;
        for (int i = 0; i != split.length; ++i) {
            path += split[i];
            Log.d(TAG, path);
            if (i == split.length-1) {
                break;
            }
            try {
                File file = new File(path);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        success = false;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                success = false;
            }
            if (!success) {
                Log.e(TAG, "Bad path!");
                break;
            }
            path += File.separatorChar;
        }
        if (success) {
            Log.i(TAG, "Writing ("+path+")...");
            try {
                String line;
                File file = new File(path);
                writer = new FileWriter(file);
                buffer = new BufferedWriter(writer);
                for (int i = 0; i != lines.size(); ++i) {
                    line = lines.get(i);
                    buffer.write(line);
                    buffer.newLine();
                    Log.d(TAG, "Written: \""+line+"\"");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                success = false;
            } finally {
                try {
                    if (buffer != null) {
                        buffer.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    success = false;
                }
            }
            if (success)
            {
                Log.i(TAG, "Wrote successfully ("+path+")!");
            }
            else
            {
                Log.e(TAG, "Wrote unsuccessfully ("+path+")!");
            }
        }
        return success;
    }

    public static List<String> toString(InputStream stream) {
        if (stream == null) {
            return null;
        }
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader buffer = new BufferedReader(reader);
        ArrayList<String> result = new ArrayList<>();
        String line = null;
        try {
            while ((line = buffer.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                buffer.close();
                reader.close();
                stream.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return result;
    }

    public static List<String> toString(Object[] objects) {
        if (objects == null) {
            return null;
        }
        Object object;
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i != objects.length; ++i) {
            object = objects[i];
            if (object == null) {
                result.add(""+null);
            }
            else {
                result.add(object.toString());
            }
        }
        return result;
    }

    public static List<String> toString(Set<Map.Entry<Object, Object>> entries) {
        if (entries == null) {
            return null;
        }
        Object object;
        Map.Entry<Object, Object> entry;
        Iterator<Map.Entry<Object, Object>> iterator = entries.iterator();
        ArrayList<String> result = new ArrayList<>();
        while (iterator.hasNext()) {
            entry = iterator.next();
            object = entry.getKey();
            if (object == null) {
                result.add(""+null);
            } else {
                result.add(object.toString());
            }
            object = entry.getValue();
            if (object == null) {
                result.add(""+null);
            } else {
                result.add(object.toString());
            }
        }
        return result;
    }

    public static String toString(List<String> strings) {
        if (strings == null) {
            return null;
        }
        String result = "";
        for (int i = 0; i != strings.size(); ++i) {
            result += strings.get(i);
            result += "\n";
        }
        return result;
    }

    public static Set<Map.Entry<Object, Object>> getEntries(Properties properties) {
        if (properties == null) {
            return null;
        }
        HashSet<Map.Entry<Object, Object>> entries = new HashSet<>();
        Iterator<String> iterator = properties.stringPropertyNames().iterator();
        String name;
        while (iterator.hasNext()) {
            name = iterator.next();
            entries.add(new AbstractMap.SimpleEntry<Object, Object>(name, properties.getProperty(name)));
        }
        return entries;
    }
}
