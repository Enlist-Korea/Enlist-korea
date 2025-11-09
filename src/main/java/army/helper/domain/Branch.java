package army.helper.domain;

import java.util.Locale;

public enum Branch {
    ARMY, NAVY, AIR, MARINE, ETC;

    public static Branch fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            return ETC;
        }

        String s = raw.trim().toLowerCase(Locale.ROOT);

        if (s.contains("육군") || s.contains("army") || s.contains("roka")) {
            return ARMY;
        } else if (s.contains("해군") || s.contains("navy")) {
            return NAVY;
        } else if (s.contains("공군") || s.contains("air")) {
            return AIR;
        } else if (s.contains("해병") || s.contains("marine")) {
            return MARINE;
        } else {
            return ETC;
        }
    }
}