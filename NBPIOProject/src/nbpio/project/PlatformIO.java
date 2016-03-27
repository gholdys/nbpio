package nbpio.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

public final class PlatformIO {
    
    
    public static enum RunCommand {
        DO_NOTHING, UPLOAD, PROGRAM
    } 
    
    private static final Logger LOGGER = Logger.getLogger( PlatformIO.class.getName() );
    
    private static final String PLATFORM_TOKEN = "Platform:";
    
    // TODO: Use which (on Linux) or where (on Windows) to find complete path to PlatformIO executable
    private static final String PLATFORMIO_COMMAND = "platformio";
    
    
    private PlatformIO() {}
    
    public static Process startProjectInitProcess( String board, String projectDirPath ) throws IOException {        
        return new ProcessBuilder().command( PLATFORMIO_COMMAND, "-f", "-c", "netbeans", "init", "--ide", "netbeans", "-b", board, "-d", projectDirPath ).start();
    }
    
    public static File addSourceFileToProject( File projectRoot, InputStream sourceFileStream, String sourceFilename ) throws IOException {                
        File srcDir = new File( projectRoot, "src" );
        srcDir.mkdirs();
        File sourceFile = new File( srcDir, sourceFilename );
        if ( !sourceFile.exists() ) {
            sourceFile.createNewFile();
            addFileToProject(sourceFileStream, sourceFile);
        }
        return sourceFile;
    }
    
    public static File addPrivateConfigFileToProject( File projectRoot, InputStream configFileStream, String configFilename ) throws IOException {        
        File nbProjectPrivateDir = new File( new File( projectRoot, "nbproject" ), "private" );
        nbProjectPrivateDir.mkdirs();
        File configFile = new File( nbProjectPrivateDir, configFilename );
        configFile.createNewFile();
        addFileToProject(configFileStream, configFile);
        return configFile;
    }
    
    public static Map<String,List<BoardDefinition>> createBoardsLookup() throws IOException {
        Process p = new ProcessBuilder().command( PLATFORMIO_COMMAND, "-f", "-c", "netbeans", "boards" ).start();
        BufferedReader reader = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
        String line;
        Map <String,List<BoardDefinition>> ret = new HashMap<>();        
        List <BoardDefinition> currentPlatformBoards = new ArrayList<>();
        BoardDefinition.Builder boardBuilder = new BoardDefinition.Builder();
        boolean parsingPlatform = false;
        try {
            while ( (line = reader.readLine()) != null ) {
                line = line.trim();
                if ( line.startsWith( PLATFORM_TOKEN ) ) {
                    String currentPlatform = line.substring(PLATFORM_TOKEN.length()).trim();
                    currentPlatformBoards = new ArrayList<>();
                    LOGGER.log(Level.INFO, "Parsing platform: {0}", currentPlatform);
                    ret.put(currentPlatform, currentPlatformBoards);
                    parsingPlatform = true;
                } else if ( parsingPlatform ) {
                    if ( line.isEmpty() ) {
                        parsingPlatform = false;
                    } else if ( !line.startsWith("-") && !line.startsWith("Type") ) {
                        String[] tokens = line.split("\\s+");
                        if ( tokens.length > 1 ) {
                            LOGGER.log(Level.INFO, "Parsing board: {0}", line);
                            StringBuilder b = new StringBuilder();
                            for ( int i=5; i<tokens.length; i++ ) {
                                b.append( tokens[i] ).append(" ");
                            }
                            currentPlatformBoards.add(
                                    boardBuilder
                                            .type( tokens[0] )
                                            .MCU( tokens[1] )
                                            .frequency( tokens[2] )
                                            .flash( tokens[3] )
                                            .RAM( tokens[4] )
                                            .name( b.toString().trim() )
                                            .build()
                            );
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            reader.close();
        }
        return ret;
    }
    
    private static void addFileToProject( InputStream sourceStream, File destFile ) throws IOException {        
        OutputStream destStream = null;
        try {            
            destStream = new FileOutputStream( destFile );
            copy(sourceStream, destStream);
        } catch (IOException ex) {
            throw ex;
        } finally {            
            try {
                if ( sourceStream != null ) sourceStream.close();
                if ( destStream != null ) destStream.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }     
    }
    
    
    private static void copy(InputStream is, OutputStream os) throws IOException {
        final byte[] buffer = new byte[65536];
        int len;

        for (;;) {
            len = is.read(buffer);
            if (len == -1) {
                return;
            }
            os.write(buffer, 0, len);
        }
    }
    
}
