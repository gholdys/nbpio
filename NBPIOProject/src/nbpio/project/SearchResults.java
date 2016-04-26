package nbpio.project;

import java.util.ArrayList;
import java.util.List;

public final class SearchResults <T> {
    
    private final List <T> results;
    private final float progress;
    
    protected SearchResults( List <T> results, float progress ) {
        this.results = new ArrayList<T> (results);
        this.progress = progress;
    }

    public List<T> getResults() {
        return results;
    }

    public float getProgress() {
        return progress;
    }
    
    public boolean isComplete() {
        return progress >= 1f;
    }
    
}
