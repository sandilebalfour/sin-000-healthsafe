package co.wethinkcode.healthsafe;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WardRepository {
    private final Map<String, Ward> store = new ConcurrentHashMap<>();

    public void saveAll(Collection<Ward> wards) {
        for (Ward w : wards) store.put(w.wardId.toUpperCase(), w);
    }
    public Collection<Ward> findAll() { return store.values(); }
    public Ward findById(String id) {
        if (id == null) return null;
        Ward w = store.get(id.toUpperCase());
        if (w == null) w = store.get(id.toUpperCase().replaceAll("[^A-Z0-9]", ""));
        return w;
    }
    public int size() { return store.size(); }
}