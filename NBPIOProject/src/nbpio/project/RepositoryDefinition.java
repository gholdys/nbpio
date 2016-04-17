package nbpio.project;

public final class RepositoryDefinition {
    
    private final String url;
    private final String type;

    public RepositoryDefinition(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }
    
    
}
