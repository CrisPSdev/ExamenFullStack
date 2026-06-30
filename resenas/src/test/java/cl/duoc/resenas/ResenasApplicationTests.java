package cl.duoc.resenas;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResenasApplicationTests {

    @Test
    void mainClass_isLoadable() {
        ResenasApplication app = new ResenasApplication();
        assertThat(app).isNotNull();
    }
}
