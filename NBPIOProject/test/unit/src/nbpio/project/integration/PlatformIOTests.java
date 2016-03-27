package nbpio.project.integration;

import nbpio.project.BoardDefinition;
import nbpio.project.PlatformIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlatformIOTests {
    
    
    @Ignore
    @Test
    public void should_parse_boards() throws IOException {
        Map<String, List<BoardDefinition>> boardsLookup = PlatformIO.createBoardsLookup();
        assertTrue("No platforms found!", boardsLookup.size() > 0);
        assertNotNull("There is no entry for atmelavr!", boardsLookup.get("atmelavr"));
        for ( Entry<String,List<BoardDefinition>> e : boardsLookup.entrySet() ) {
            assertTrue("Board list for " + e.getKey() + " is empty", e.getValue().size() > 0);
        }
    }
    
    public void should_create_a_new_platformio_project() throws IOException {
        Path tempDir = Files.createTempDirectory("platformio");        
        String projectDirPath = tempDir.toString();
        
        ProcessBuilder b = new ProcessBuilder().command("platformio", "-f", "-c", "netbeans", "init", "--ide", "netbeans", "-b", "uno", "-d", projectDirPath);
        b.environment().put("TERM", "xterm");
        Process p = b.start();
        
//        Process p = PlatformIO.startProjectInitProcess("uno", projectDirPath);
        
        BufferedReader errorReader = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );        
        List <String> lines = new ArrayList<>();
        String line;
        while ( (line = errorReader.readLine()) != null ) {
            lines.add(line);
        }
        Files.write( new File( new File(projectDirPath), "err.txt" ).toPath(), lines );
        
//        BufferedReader outputReader = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
//        lines.clear();        
//        while ( (line = outputReader.readLine()) != null ) {
//            lines.add(line);
//        }
//        Files.write( new File( new File(projectDirPath), "out.txt" ).toPath(), lines );

        int c;
        InputStream r = p.getInputStream();
        while ( ( c = r.read() ) != -1 ) {
            System.out.print( (char) c );
        }
        
        assertTrue("Command failed! Error code: " + p.exitValue(), p.exitValue() == 0);        
        
        // Remove the temp dir
        removeDirectoryTree(tempDir);
    }
    
    private static void removeDirectoryTree( Path dir ) throws IOException {
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
    }
    
    
}
