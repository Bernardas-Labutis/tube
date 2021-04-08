package lt.vu.tube.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class TestPost {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private TestUser creator;

    private String text;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public TestUser getCreator() {
        return creator;
    }

    public void setCreator(TestUser creator) {
        this.creator = creator;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
