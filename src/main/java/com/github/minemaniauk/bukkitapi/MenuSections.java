package com.github.minemaniauk.bukkitapi;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.excludeId;

public final class MenuSections {

    /** Returns the subdocument under menu.<path> as a plain Map (or empty map if missing). */
    public static Map<String,Object> loadMenuSectionMap(MongoCollection<Document> col, String path) {
        String projPath = "menu." + path; // e.g. menu.server.main
        Document result = col.find(eq("_id", "Menu_Servers"))
                .projection(fields(include(projPath), excludeId()))
                .first();
        if (result == null) return Map.of();

        // Walk down: menu -> keys in path
        Object cur = result.get("menu");
        for (String key : path.split("\\.")) {
            if (!(cur instanceof Document)) return Map.of();
            cur = ((Document) cur).get(key);
        }
        if (!(cur instanceof Document)) return Map.of();

        return toPlainMap((Document) cur);
    }

    // ---- helpers: Document/List -> plain Map/List ----
    @SuppressWarnings("unchecked")
    private static Map<String,Object> toPlainMap(Map<String,Object> src) {
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<String,Object> e : src.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map) {
                out.put(e.getKey(), toPlainMap((Map<String,Object>) v));
            } else if (v instanceof List) {
                out.put(e.getKey(), toPlainList((List<?>) v));
            } else {
                out.put(e.getKey(), v);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> toPlainList(List<?> list) {
        List<Object> out = new ArrayList<>(list.size());
        for (Object v : list) {
            if (v instanceof Map) out.add(toPlainMap((Map<String,Object>) v));
            else if (v instanceof List) out.add(toPlainList((List<?>) v));
            else out.add(v);
        }
        return out;
    }
}
