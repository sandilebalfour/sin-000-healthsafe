package co.wethinkcode.healthsafe;

import org.junit.Test;
//import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class EmergencyStatusTest {

    @Test
    public void testNoArgsConstructor() {
        EmergencyStatus status = new EmergencyStatus();
        assertEquals(0, status.getLevel()); // default int value
        assertNull(status.getCode()); // code not set yet
    }

    @Test
    public void testArgsConstructor(){
        EmergencyStatus em = new EmergencyStatus(4);
        assertEquals(4, em.getLevel());
        assertEquals(Code.RED, em.getCode());
    }

    @Test
    public void testSetLevelUpdatesCode(){
        EmergencyStatus em = new EmergencyStatus();
        em.setLevel(3);
        assertEquals(3, em.getLevel());
        assertEquals(Code.PINK, em.getCode());
    }
    @Test
    public void testInvalidLevelThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EmergencyStatus.getCodeLevel(9);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            EmergencyStatus.getCodeLevel(-1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new EmergencyStatus(100);
        });
    }

}
