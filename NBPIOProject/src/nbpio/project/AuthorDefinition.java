package nbpio.project;

public final class AuthorDefinition {
    
    private final String url;
    private final boolean maintainer;
    private final String name;
    private final String email;

    public AuthorDefinition(String url, boolean maintainer, String name, String email) {
        this.url = url;
        this.maintainer = maintainer;
        this.name = name;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public boolean isMaintainer() {
        return maintainer;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }    
    
}
