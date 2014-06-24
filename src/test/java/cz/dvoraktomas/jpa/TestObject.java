package cz.dvoraktomas.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TestObject {
    @Id
    private String username;
    private String email;

    TestObject() {
    }

    public TestObject(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


