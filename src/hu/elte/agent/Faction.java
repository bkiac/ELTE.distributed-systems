package hu.elte.agent;

import java.util.HashMap;
import java.util.Map;

public enum Faction {

    CIA(1, "CIA"),
    KGB(2, "KGB");

    private final int id;
    private final String name;

    private static final Map<String, Faction> LOOKUP = new HashMap<>();

    static {
        for (Faction f : Faction.values()) {
            LOOKUP.put(f.getName(), f);
        }
    }

    Faction(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Faction getFactionByName(String name) {
        return LOOKUP.get(name);
    }
}
