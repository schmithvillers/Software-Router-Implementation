
package router;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouterApplicationTest {

    @Test
    void shouldExposeApplicationName() {
        assertEquals(
                "Java Software Router",
                RouterApplication.applicationName()
        );
    }
}
