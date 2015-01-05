package du.tools.main.stash;


import java.util.List;

public class StashProject {
    private String name;
    private String key;
    private String description;
    private List<String> repositories;

    public StashProject(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }
}
