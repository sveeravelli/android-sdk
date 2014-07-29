/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui.storageviewer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Pair;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.accessenabler.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageHelper {
    private static final String LOG_TAG = "StorageHelper";

    public static final int CURRENT_STORAGE_VERSION = 3;

    private static final int MAX_STRING_LENGTH = 100;

    private static final String SQL_STORAGE_TABLE_NAME = "storage";
    private static final String SQL_CACHE_TABLE_NAME = "cache";

    private static final String SQL_STORAGE_ID = "_id";
    private static final String SQL_STORAGE_KEY = "key";
    private static final String SQL_STORAGE_VALUE = "value";

    private static final String CAN_AUTHENTICATE_KEY = "canAuthenticate";
    private static final String CURRENT_MVPD_ID_KEY = "currentMvpdId";
    private static final String AUTHN_TOKEN_KEY = "authnToken";
    private static final String METADATA_TOKEN_KEY = "metadataToken";
    private static final String PREAUTHORIZATION_CACHE_KEY = "preauthorizationCache";

    private File externalStorage;
    private SQLiteDatabase database = null;

    class TreeNode {
        private long id;
        private int level;
        private String label;

        public TreeNode(int id, int level, String label) {
            this.id = id;
            this.level = level;
            this.label = label;
        }

        public long getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public String getLabel() {
            return label;
        }

        public String toString() {
            return id + " | " + level + " | " + label;
        }
    }

    public StorageHelper() {
        externalStorage = Environment.getExternalStorageDirectory();
    }

    public void clearStorage(int version) {
        try {
            String storagePath = buildStorageFileName(version);

            if (version >= CURRENT_STORAGE_VERSION) {
                FileOutputStream fileOut = new FileOutputStream(storagePath);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(new HashMap());
                out.close();
                fileOut.close();
            } else {
                File storageFile = new File(storagePath);
                if (storageFile.exists())
                    if (!storageFile.delete())
                        Log.d(LOG_TAG, "Error clearing storage");
            }
        } catch(Exception e) {
            Log.d(LOG_TAG, "Error clearing storage: " + e.toString());
        }
    }

    public void clearStorageAll() {
        for (int v = 1; v <= CURRENT_STORAGE_VERSION; v++)
            clearStorage(v);
    }

    public List<TreeNode> readStorage(int version) {
        List<TreeNode> nodes = (version >= CURRENT_STORAGE_VERSION) ?
            readStorageNew(version) : readStorageOld(version);

        if (nodes.size() == 0)
            nodes.add(new TreeNode(0, 0, "Empty storage"));

        return nodes;
    }

    private List<TreeNode> readStorageNew(int version) {
        List<TreeNode> nodes = new ArrayList<TreeNode>();
        Map storageMap;
        try {
            String storagePath = buildStorageFileName(version);
            FileInputStream fileIn = new FileInputStream(storagePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            storageMap = (Map) in.readObject();
            in.close();
            fileIn.close();

            traverseTree(storageMap, 0, nodes);
        } catch(Exception e) {
            Log.d(LOG_TAG, "Error while reading from storage: " + e.toString());
        }

        return nodes;
    }

    private List<TreeNode> readStorageOld(int version) {
        List<TreeNode> nodes = new ArrayList<TreeNode>();

        if (!openDatabase(version))
            return nodes;

        Map storageMap = extractSqlStorageData();
        traverseTree(storageMap, 0, nodes);

        return nodes;
    }

    private boolean openDatabase(int version) {
        String databasePath = buildStorageFileName(version);

        Cursor dbCursor = null;

        try {
            database = SQLiteDatabase.openDatabase(databasePath, null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);

            dbCursor = database.query(
                    SQL_STORAGE_TABLE_NAME, new String[]{
                    SQL_STORAGE_ID, SQL_STORAGE_KEY, SQL_STORAGE_VALUE},
                    null, null, null, null, null, null);
            dbCursor.close();

            dbCursor = database.query(
                    SQL_CACHE_TABLE_NAME, new String[]{
                    SQL_STORAGE_ID, SQL_STORAGE_KEY, SQL_STORAGE_VALUE},
                    null, null, null, null, null, null);
            dbCursor.close();
        } catch (SQLiteException e) {
            Log.d(LOG_TAG, "Error initializing database: " + e.toString());
            return false;
        } finally {
            if (dbCursor != null) {
                dbCursor.close();
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Map extractSqlStorageData() {
        Map resultMap = new HashMap();

        HashMap<String, Pair<String, String>> map = sqlSelectAll(SQL_STORAGE_TABLE_NAME);
        for (Pair<String, String> entry : map.values()) {
            if (entry.first.equals(CAN_AUTHENTICATE_KEY)) {
                boolean canAuthenticate = (entry.second != null) && (entry.second.equals("TRUE"));
                resultMap.put(entry.first, canAuthenticate);
            } else if (entry.first.equals(CURRENT_MVPD_ID_KEY)) {
                resultMap.put(entry.first, entry.second);
            } else if (entry.first.contains(AUTHN_TOKEN_KEY)) {
                String tokenXml = new String(Utils.base64Decode(entry.second));
                resultMap.put(entry.first, tokenXml);
            } else if (entry.first.contains(METADATA_TOKEN_KEY)) {
                String tokenJson = new String(Utils.base64Decode(entry.second));
                resultMap.put(entry.first, tokenJson);
            } else {
                String resourceId = new String(Utils.base64Decode(entry.first));
                String tokenXml = new String(Utils.base64Decode(entry.second));
                resultMap.put(resourceId, tokenXml);
            }
        }

        map = sqlSelect(PREAUTHORIZATION_CACHE_KEY, SQL_CACHE_TABLE_NAME);
        String preauthorizedCacheXml = null;
        for (Pair<String, String> entry : map.values()) {
            preauthorizedCacheXml = new String(Utils.base64Decode(entry.second));
        }
        resultMap.put(PREAUTHORIZATION_CACHE_KEY, preauthorizedCacheXml);

        return resultMap;
    }

    private HashMap<String, Pair<String, String>> sqlSelectAll(String tableName) {
        Cursor cursor = database.query(
                true, tableName, new String[]{
                SQL_STORAGE_ID, SQL_STORAGE_KEY, SQL_STORAGE_VALUE},
                null, null, null, null, null, null);

        HashMap<String, Pair<String, String>> map = new HashMap<String, Pair<String, String>>();

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                map.put(cursor.getString(0), new Pair<String, String>(cursor.getString(1), cursor.getString(2)));
                cursor.moveToNext();
            }

            cursor.close();
        }

        return map;
    }

    private HashMap<String, Pair<String, String>> sqlSelect(String key, String tableName) {
        Cursor cursor = database.query(
                true, tableName, new String[]{
                SQL_STORAGE_ID, SQL_STORAGE_KEY, SQL_STORAGE_VALUE},
                SQL_STORAGE_KEY + "='" + key + "'", null, null, null, null, null);

        HashMap<String, Pair<String, String>> map = new HashMap<String, Pair<String, String>>();

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                map.put(cursor.getString(0), new Pair<String, String>(cursor.getString(1), cursor.getString(2)));
                cursor.moveToNext();
            }

            cursor.close();
        }

        return map;
    }

    private void traverseTree(Object currentNode, int level, List<TreeNode> nodes) {
        if (currentNode == null)
            return;

        if (isPrimitiveValue(currentNode)) {
            nodes.add(new TreeNode(nodes.size(), level, String.valueOf(currentNode)));
        } else if (currentNode instanceof Map) {
            Map map = (Map) currentNode;
            for (Object key : map.keySet()) {
                if (!(key instanceof String))
                    continue;

                Object value = map.get(key);
                if (value == null || (isPrimitiveValue(value) && String.valueOf(value).length() < MAX_STRING_LENGTH))
                    nodes.add(new TreeNode(nodes.size(), level, String.valueOf(key) + ": " +
                            (value != null ? String.valueOf(value) : "")));
                else {
                    nodes.add(new TreeNode(nodes.size(), level, String.valueOf(key)));
                    traverseTree(value, level + 1, nodes);
                }
            }
        } else if (currentNode instanceof List) {
            List list = (List) currentNode;
            for (int i = 0; i < list.size(); i++) {
                Object value = list.get(i);
                if (value == null || (isPrimitiveValue(value) && String.valueOf(value).length() < MAX_STRING_LENGTH))
                    nodes.add(new TreeNode(nodes.size(), level, String.valueOf(i) + ": " +
                            (value != null ? String.valueOf(value) : "")));
                else {
                    nodes.add(new TreeNode(nodes.size(), level, String.valueOf(i)));
                    traverseTree(value, level + 1, nodes);
                }
            }
        }
    }

    private boolean isPrimitiveValue(Object value) {
        return value instanceof String || value instanceof Integer ||
                value instanceof Long || value instanceof Boolean;
    }

    private String buildStorageFileName(int version) {
        final String DATABASE_FILENAME = ".adobepassdb";
        return externalStorage.getPath() + File.separator +
                DATABASE_FILENAME + (version == 0 ? "" : "_" + version);
    }
}
