package nbpio.project;

public final class LibraryDefinition {
    
    private final int id;
    private final String name;
    private final AuthorDefinition[] authors;
    private final String description;
    private final RepositoryDefinition repository;
    private final String[] frameworks;
    private final String[] platforms;
    private final String[] keywords;
    private final String version;

    private LibraryDefinition( Builder src ) {
        this.id = src.id;
        this.name = src.name;        
        this.repository = src.repository;
        this.authors = src.authors;
        this.frameworks = src.frameworks;
        this.platforms = src.platforms;
        this.keywords = src.keywords;
        this.description = src.description;
        this.version = src.version;
    }

    public AuthorDefinition[] getAuthors() {
        return authors;
    }

    public String[] getFrameworks() {
        return frameworks;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String[] getPlatforms() {
        return platforms;
    }

    public RepositoryDefinition getRepository() {
        return repository;
    }

    public String getVersion() {
        return version;
    }
    
    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    
    public static class Builder {
        
        private int id;
        private String name;
        private RepositoryDefinition repository;
        private String description;
        private AuthorDefinition[] authors;        
        private String[] frameworks;
        private String[] platforms;
        private String[] keywords;
        private String version;
        
        
        public Builder() {}
        
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        
        public Builder authors( AuthorDefinition[] authors ) {
            this.authors = authors;
            return this;
        }
        
        public Builder platforms( String[] platforms ) {
            this.platforms = platforms;
            return this;
        }
        
        public Builder frameworks( String[] frameworks ) {
            this.frameworks = frameworks;
            return this;
        }
        
        public Builder keywords( String[] keywords ) {
            this.keywords = keywords;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder repository(RepositoryDefinition repository) {
            this.repository = repository;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public LibraryDefinition build() {
            return new LibraryDefinition( this );
        }
        
    }
    
}
