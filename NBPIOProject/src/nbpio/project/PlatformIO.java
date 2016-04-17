package nbpio.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public final class PlatformIO {
    
    private static final Logger LOGGER = Logger.getLogger( PlatformIO.class.getName() );
    
    private static final String PLATFORM_TOKEN = "Platform:";
    
    private static final String INI_FILENAME = "platformio.ini";
    
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
    
    public static List<LibraryDefinition> createLibrariesList() throws IOException {        
        Process p = new ProcessBuilder().command( PLATFORMIO_COMMAND, "-f", "-c", "netbeans", "lib", "list", "--json-output" ).start();                
        
        BufferedReader reader = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
        JSONParser parser = new JSONParser();
        List <LibraryDefinition> ret = new ArrayList<>();
        try {
            Object jsonObject = parser.parse( reader );
            if ( jsonObject instanceof JSONArray ) {
                JSONArray array = (JSONArray) jsonObject;
                array.forEach( (e) -> ret.add( parseLibraryJSONObject((JSONObject) e) ) );
            }
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Failed to parse libraries list", ex);
            return null;
        } finally {
            reader.close();
        }
        
        return ret;
    }
    
    // TODO: Use PlatformIO "search" function instead of going through listed libraries
    public static List<LibraryDefinition> findMatchingLibraries( String searchTerm ) throws IOException {
        String lowerCaceSearchWord = searchTerm.toLowerCase();
        List <LibraryDefinition> allLibs = createLibrariesList();
        return allLibs.stream().filter( (lib) -> {
            return lib.getName().toLowerCase().contains(lowerCaceSearchWord) || lib.getDescription().toLowerCase().contains(lowerCaceSearchWord);
        }).collect( Collectors.toList() );
    }
    
    public static LibraryDefinition parseLibraryJSONObject( JSONObject obj ) {
        return new LibraryDefinition.Builder()
            .name( getStringFromJSON(obj, "name") )
            .repository( getRepositoryDefinitionFromJSON(obj, "repository") )
            .frameworks( getStringArrayFromJSON(obj, "frameworks") )
            .platforms( getStringArrayFromJSON(obj, "platforms") )
            .version( getStringFromJSON(obj, "version") )
            .authors( getAuthorDefinitionsFromJSON(obj, "authors") )            
            .keywords( getStringArrayFromJSON(obj, "keywords") )
            .id( getIntegerFromJSON(obj, "id") )            
            .description( getStringFromJSON(obj, "description") )            
            .build();
    }
    
    public static boolean isPlatformIOProject( File projectRoot ) {
        try {
            getProjectIniFile(projectRoot);
            return true;
        } catch (FileNotFoundException ex) {
            return false;
        }
    }
    
    public static String getProjectPlatform( File projectRoot ) {
        try {
            List<String> projectIniFileContents = getProjectIniFileContents(projectRoot);
            Optional<String> platformEntry = projectIniFileContents.stream().filter( (line) -> line.startsWith("platform =") ).findAny();
            if ( platformEntry.isPresent() ) {
                return platformEntry.get().split("=")[1].trim();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to load project ini file", ex);            
        }
        return null;
    }
    
    
    // ******************************************************
    // ************* PRIVATE HELPER METHODS *****************
    // ******************************************************    
    private static File getProjectIniFile( File projectRoot ) throws FileNotFoundException {
        File iniFile = new File( projectRoot, INI_FILENAME);
        if ( iniFile.exists() ) {
            return iniFile;
        } else {
            throw new FileNotFoundException( INI_FILENAME + " not found!");
        }
    }
    
    private static List<String> getProjectIniFileContents( File projectRoot ) throws IOException {
        File iniFile = getProjectIniFile(projectRoot);        
        return Files.readAllLines( iniFile.toPath() );
    }
    
    private static String getStringFromJSON( JSONObject obj, String field ) {
        Object value = obj.get( field );
        return value != null ? value.toString() : null;
    }
    
    private static int getIntegerFromJSON( JSONObject obj, String field ) {
        Object value = obj.get( field );
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }
    
    private static RepositoryDefinition getRepositoryDefinitionFromJSON( JSONObject obj, String field ) {
        Object value = obj.get( field );
        if ( value instanceof JSONObject ) {
            JSONObject jsonObject = (JSONObject) value;
            String url = jsonObject.get("url").toString();
            String type = jsonObject.get("type").toString();
            return new RepositoryDefinition(url, type);
        } else {
            LOGGER.log( Level.WARNING, "The \"" + field + "\" field does not contain a repository definition.");
            return null;
        }
    }
    
    private static AuthorDefinition[] getAuthorDefinitionsFromJSON( JSONObject obj, String field ) {
        Object value = obj.get( field );
        if ( value instanceof JSONArray ) {            
            JSONArray jsonArray = (JSONArray) value;
            AuthorDefinition[] ret = new AuthorDefinition[jsonArray.size()];
            for ( int i=0; i<ret.length; i++ ) {
                JSONObject authorEntry = (JSONObject) jsonArray.get(i);
                ret[i] = new AuthorDefinition(
                    getStringFromJSON( authorEntry, "url"),
                    authorEntry.get("maintainer").toString().equals("true"),
                    getStringFromJSON( authorEntry, "name"),
                    getStringFromJSON( authorEntry, "email")
                );
            }
            return ret;
        } else {
            LOGGER.log( Level.WARNING, "The \"" + field + "\" field does not contain a author definitions.");
            return null;
        }
    }
    
    private static String[] getStringArrayFromJSON( JSONObject obj, String field ) {
        Object value = obj.get( field );
        if ( value instanceof JSONArray ) {
            JSONArray array = (JSONArray) value;
            String[] ret = new String[array.size()];
            for ( int i=0; i<ret.length; i++ ) {
                ret[i] = array.get(i).toString();
            }
            return ret;
        }
        return null;
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
                LOGGER.log( Level.WARNING, "Failed to close streams", ex );
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
