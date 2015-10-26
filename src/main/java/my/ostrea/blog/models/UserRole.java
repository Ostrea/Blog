package my.ostrea.blog.models;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "role", "my_user_id" }))
public class UserRole {

    @Id
    @GeneratedValue
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private MyUser myUser;

    @Column(nullable = false, length = 45)
    private String role;

    protected UserRole() {
    }

    public UserRole(MyUser myUser, String role) {
        this.myUser = myUser;
        this.role = role;
    }

    public MyUser getMyUser() {
        return this.myUser;
    }

    public void setMyUser(MyUser myUser) {
        this.myUser = myUser;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}