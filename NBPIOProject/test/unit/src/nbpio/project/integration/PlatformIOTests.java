package nbpio.project.integration;

import java.io.ByteArrayInputStream;
import java.io.File;
import nbpio.project.BoardDefinition;
import nbpio.project.PlatformIO;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import nbpio.project.LibraryDefinition;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class PlatformIOTests {

    private Path tempDir;

    @Before
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("platformio");
        System.out.println("Created temporary directory: " + tempDir);
    }

    @After
    public void cleanup() {
        removeDirectoryTree(tempDir);
        System.out.println("Removed temporary directory: " + tempDir);
    }

    @Test
    public void should_create_boards_lookup_table() throws IOException {
        Map<String, List<BoardDefinition>> boardsLookup = PlatformIO.createBoardsLookup();
        assertTrue("No platforms found!", boardsLookup.size() > 0);
        assertNotNull("There is no entry for atmelavr!", boardsLookup.get("atmelavr"));
        for (Entry<String, List<BoardDefinition>> e : boardsLookup.entrySet()) {
            assertTrue("Board list for " + e.getKey() + " is empty", e.getValue().size() > 0);
        }
    }

    @Test
    public void should_create_a_new_platformio_project_and_verify_platform() throws IOException, InterruptedException {
        // Given
        String projectDirPath = tempDir.toString();
        String platform = "atmelavr";
        
        // When
        Process p = PlatformIO.startProjectInitProcess("uno", projectDirPath);
        p.waitFor();

        // Then
        assertTrue( "PlatformIO command failed! Error code: " + p.exitValue(), p.exitValue() == 0 );
        assertEquals( "Wrong project platform!", platform, PlatformIO.getProjectPlatform( new File(projectDirPath) ) );
    }

    @Test
    public void should_add_a_source_file_to_project_tree() throws IOException {
        // Given
        String sourceFilename = "source.cpp";
        byte[] sourceFileContents = "Sample source file contents".getBytes();
        String projectDirPath = tempDir.toString();
        File sourceFile = new File(projectDirPath + "/src/" + sourceFilename);
        ByteArrayInputStream sourceFileStream = new ByteArrayInputStream(sourceFileContents);
        
        // When
        PlatformIO.addSourceFileToProject( new File(projectDirPath), sourceFileStream, sourceFilename);
        
        // Then
        assertTrue( "Source file not found!", sourceFile.exists() );
        assertTrue( "Source file contents is not correct!", Arrays.equals( Files.readAllBytes( sourceFile.toPath() ), sourceFileContents) );
    }
    
    @Test
    public void should_add_a_config_file_to_project_tree() throws IOException {
        // Given
        String configFilename = "config.properties";
        byte[] configFileContents = "Sample config file contents".getBytes();
        String projectDirPath = tempDir.toString();
        File configFile = new File(projectDirPath + "/nbproject/private/" + configFilename);
        ByteArrayInputStream configFileStream = new ByteArrayInputStream(configFileContents);
        
        // When
        PlatformIO.addPrivateConfigFileToProject(new File(projectDirPath), configFileStream, configFilename);
        
        // Then
        assertTrue( "Config file not found!", configFile.exists() );
        assertTrue( "Config file contents is not correct!", Arrays.equals( Files.readAllBytes( configFile.toPath() ), configFileContents) );
    }

    @Test
    public void should_parse_libraries_list() throws IOException {
        /*
        [
            {
                "updated": "2016-02-23T10:50:02Z", 
                "description": "Arduino library for HDC1000 and HDC1008 sensors", 
                "frameworks": ["arduino"], 
                "dlmonth": 2, 
                "examplenums": 1, 
                "authornames": ["Adafruit Industries"], 
                "platforms": ["atmelavr", "atmelsam"], 
                "keywords": ["sensor", "humidity", "temperature"], 
                "id": 1102, 
                "name": "Adafruit-HDC1000"
            }
        ]
        */
        
        List <LibraryDefinition> libraries = PlatformIO.listInstalledLibraries();
        for ( LibraryDefinition libraryDefinition : libraries ) {
            assertNotNull( libraryDefinition.getId() );
            assertNotNull( libraryDefinition.getName() );            
            assertNotNull( libraryDefinition.getAuthors() );            
            assertNotNull( libraryDefinition.getPlatforms() );
        }
    }
    
    @Test
    public void should_find_libraries_with_given_search_term() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List <LibraryDefinition> allResults = new ArrayList<>();
        PlatformIO.startLibrarySearch("humidity", (r) -> {
            allResults.addAll( r.getResults() );
            if ( r.isComplete() ) {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
        assertTrue( allResults.size() > 0 );
    }
    
    private static void removeDirectoryTree( Path dir ) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            fail( ex.getMessage() );
        }
    }   
    
}
