package me.deathrealms.inventoryhistory;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class InventoryUtils {

    public static String objectToString(Object object) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(stream);
            data.writeObject(object);
            data.flush();
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Object stringToObject(String contentsData) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(contentsData));
            BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            return data.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
