package lang.toyscript.engine;

import org.junit.jupiter.api.Test;

import javax.script.ScriptEngineManager;

import static org.assertj.core.api.Assertions.assertThat;

public class ToyScriptEngineFactoryTest {

    @Test
    public void shouldGetEngineByShortName() {
        // given
        var manager = new ScriptEngineManager();

        // when
        var engine1 = manager.getEngineByName("toys");
        var engine2 = manager.getEngineByName("toyscript");
        var engine3 = manager.getEngineByName("ToyScript");

        //then
        assertThat(engine1).isNotNull().isInstanceOf(ToyScriptEngine.class);
        assertThat(engine2).isNotNull().isInstanceOf(ToyScriptEngine.class);
        assertThat(engine3).isNotNull().isInstanceOf(ToyScriptEngine.class);
    }

    @Test
    public void shouldNotGetEngineByMisspelledName() {
        // given
        var manager = new ScriptEngineManager();

        // when
        var engine = manager.getEngineByName("toyscr");

        // then
        assertThat(engine).isNull();
    }
}