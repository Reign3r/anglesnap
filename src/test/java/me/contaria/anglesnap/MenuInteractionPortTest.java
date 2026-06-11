package me.contaria.anglesnap;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuInteractionPortTest {
    private static final Path PROJECT_DIR = Path.of(System.getProperty("user.dir"));

    @Test
    void interactiveListsUseContainerObjectSelectionList() throws IOException {
        assertUsesContainerList("src/main/java/me/contaria/anglesnap/gui/screen/AngleSnapListWidget.java");
        assertUsesContainerList("src/main/java/me/contaria/anglesnap/gui/camerasnap/CameraSnapListWidget.java");
        assertUsesContainerList("src/main/java/me/contaria/anglesnap/gui/config/AngleSnapConfigListWidget.java");
    }

    @Test
    void interactiveRowsExposeChildrenAndNarratables() throws IOException {
        assertExposesChildren("src/main/java/me/contaria/anglesnap/gui/screen/AngleSnapListWidget.java");
        assertExposesChildren("src/main/java/me/contaria/anglesnap/gui/camerasnap/CameraSnapListWidget.java");
        assertExposesChildren("src/main/java/me/contaria/anglesnap/gui/config/AngleSnapConfigListWidget.java");
    }

    @Test
    void addRowsReturnChildClickResult() throws IOException {
        String angleList = readSource("src/main/java/me/contaria/anglesnap/gui/screen/AngleSnapListWidget.java");
        String cameraList = readSource("src/main/java/me/contaria/anglesnap/gui/camerasnap/CameraSnapListWidget.java");

        assertTrue(angleList.contains("return super.mouseClicked(click, doubled);"));
        assertTrue(cameraList.contains("return super.mouseClicked(click, doubled);"));
        assertFalse(angleList.contains("super.mouseClicked(click, doubled);\n            return false;"));
        assertFalse(cameraList.contains("super.mouseClicked(click, doubled);\n            return false;"));
    }

    @Test
    void menuRowsPersistSaveAndDeleteActionsExplicitly() throws IOException {
        String angleList = readSource("src/main/java/me/contaria/anglesnap/gui/screen/AngleSnapListWidget.java");
        String cameraList = readSource("src/main/java/me/contaria/anglesnap/gui/camerasnap/CameraSnapListWidget.java");
        String cameraScreen = readSource("src/main/java/me/contaria/anglesnap/gui/camerasnap/CameraSnapScreen.java");

        assertTrue(angleList.contains("AngleSnap.CONFIG.saveAngles();"));
        assertTrue(cameraList.contains("AngleSnap.CONFIG.saveCameraPositions();"));
        assertTrue(cameraScreen.contains("AngleSnap.CONFIG.saveCameraPositions();"));
        assertFalse(cameraScreen.contains("AngleSnap.CONFIG.saveAngles();"));
    }

    @Test
    void cameraCoordinateValidationAcceptsDoublePrecisionValues() throws IOException {
        String cameraList = readSource("src/main/java/me/contaria/anglesnap/gui/camerasnap/CameraSnapListWidget.java");

        assertTrue(cameraList.contains("Double.parseDouble(text);"));
        assertFalse(cameraList.contains("Float.parseFloat(text);"));
    }

    private static void assertUsesContainerList(String relativePath) throws IOException {
        String source = readSource(relativePath);

        assertTrue(source.contains("import net.minecraft.client.gui.components.ContainerObjectSelectionList;"));
        assertFalse(source.contains("import net.minecraft.client.gui.components.ObjectSelectionList;"));
        assertTrue(source.contains("extends ContainerObjectSelectionList<"));
    }

    private static void assertExposesChildren(String relativePath) throws IOException {
        String source = readSource(relativePath);

        assertTrue(source.contains("List<? extends GuiEventListener> children()"));
        assertTrue(source.contains("List<? extends NarratableEntry> narratables()"));
        assertTrue(source.contains("return this.children;"));
    }

    private static String readSource(String relativePath) throws IOException {
        return Files.readString(PROJECT_DIR.resolve(relativePath)).replace("\r\n", "\n");
    }
}
