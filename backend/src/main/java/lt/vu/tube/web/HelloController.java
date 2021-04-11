package lt.vu.tube.web;

import lt.vu.tube.entity.TestPost;
import lt.vu.tube.entity.TestUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@RestController
public class HelloController {

    @Autowired
    EntityManager entityManager;

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    //Remove stuff below later
    @RequestMapping("/user/{id}")
    public String getUser(@PathVariable Long id) {
        TestUser user = entityManager.find(TestUser.class, id);

        return "Greetings from " + (user != null ? user.getName() : "null");
    }
    @RequestMapping("/user")

    @Transactional
    public String newUser(@RequestParam String name, @RequestParam String lastName) {
        TestUser user = new TestUser();
        user.setName(name);
        user.setLastName(lastName);
        entityManager.persist(user);

        return "Greetings from " + name + " " + user.getLastName() + " " + user.getId() ;
    }

    @RequestMapping("/user/post/{id}")
    @Transactional
    public String newPost(@PathVariable Long id, @RequestParam String text) {
        TestUser user = entityManager.find(TestUser.class, id);
        if (user == null) {
            return "User not found!";
        }
        else {
            TestPost post = new TestPost();
            post.setText(text);
            post.setCreator(user);
            user.getPosts().add(post);
            entityManager.persist(user);
            return "Greetings from " + user.getName() + " " + user.getLastName() + " " + user.getId() + " " + post.getId();

        }
    }

    @RequestMapping("/user/posts/{id}")
    public String getPosts(@PathVariable Long id) {
        TestUser user = entityManager.find(TestUser.class, id);
        if (user == null) {
            return "User not found!";
        }
        return user.getPosts().stream().map(post -> post.getText() + " " + post.getId() + " " + post.getCreator().getId()).reduce("", (a,b)->a + "<br>" + b);
    }
}