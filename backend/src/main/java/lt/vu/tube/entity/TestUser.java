package lt.vu.tube.entity;

import javax.persistence.*;
import java.util.List;

@Entity(name="TEST")
public class TestUser {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String lastName;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TestPost> posts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<TestPost> getPosts() {
        return posts;
    }

    public void setPosts(List<TestPost> posts) {
        this.posts = posts;
    }
}
