package decodes.launcher;

import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.exception.ActionFailedException;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.fixture.JTableCellFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.timing.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import fixtures.GuiTest;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
public class ProfileManagerFrameTest extends GuiTest
{
    @SystemStub
    private static SystemProperties properties = new SystemProperties(System.getProperties());

    @TempDir
    private static File userDir;

    ProfileManagerFrame pmf;
    private FrameFixture frame;

    @BeforeEach
    public void setup() throws Exception
    {
        String resourceDir = System.getProperty("resource.dir");
        properties.set("DCSTOOL_USERDIR",resourceDir+"/decodes/launcher/profiles");
        pmf = GuiActionRunner.execute(() -> new ProfileManagerFrame());
        pmf.setExitOnClose(false);
        frame = new FrameFixture(pmf);
        frame.show();
        pause(new Condition("Gui visible") {
            @Override
            public boolean test() {
                return execute(pmf::isVisible);
            }
        },timeout(500));
    }

    @Test
    public void test_profile_manager() throws Exception
    {
        JTableFixture jtf = frame.table("profileTable");
        JTableCellFixture tc = jtf.cell("cwms");
        tc.select();
        frame.button("copyProfile").click();
        JOptionPaneFixture opf = frame.optionPane();
        opf.textBox().setText("cwms2");
        opf.okButton().click();
        JTableCellFixture cwms2 = jtf.cell("cwms2").requireNotEditable();

        frame.button("copyProfile").click();
        opf = frame.optionPane();
        opf.textBox().setText("cwms2");
        opf.okButton().click();

        opf = frame.optionPane();
        opf.requireTitle("Error!");
        opf.requireMessage(Pattern.compile("A profile with that name already.*"));
        opf.okButton().click();

        cwms2.select();
        frame.button("deleteProfile").click();
        opf = frame.optionPane();
        opf.yesButton().click();
        assertThrows(ActionFailedException.class, () ->
        {
            jtf.cell("cwms2").requireNotEditable();
        });

        jtf.tableHeader().clickColumn(0);
        JTableCellFixture userProfileCell = jtf.cell("(default)");
        assertEquals(0,userProfileCell.row());
        userProfileCell.select();
        frame.button("deleteProfile").click();
        opf = frame.optionPane();
        opf.requireMessage(Pattern.compile("Cannot delete the default profile!"));
        opf.okButton().click();
    }

    @AfterEach
    public void tearDown()
    {
        frame.cleanUp();
    }
}