package com.app.questofseoul.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MissionTypeTest {

    @Test
    void fromApiValue_parsesSupportedTypes() {
        assertEquals(MissionType.QUIZ, MissionType.fromApiValue("quiz"));
        assertEquals(MissionType.OX, MissionType.fromApiValue("OX"));
        assertEquals(MissionType.PHOTO, MissionType.fromApiValue("photo"));
        assertEquals(MissionType.TEXT_INPUT, MissionType.fromApiValue("text_input"));
    }

    @Test
    void fromApiValue_rejectsLegacyAliases() {
        assertThrows(IllegalArgumentException.class, () -> MissionType.fromApiValue("INPUT"));
        assertThrows(IllegalArgumentException.class, () -> MissionType.fromApiValue("PHOTO_CHECK"));
    }
}
