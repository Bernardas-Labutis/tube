package lt.vu.tube.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class UserActivityLog {
    private Long id;
    private String username;
    private String permissions;
    private Timestamp executionDate;
    private String executedMethod;

    public UserActivityLog() {}

    public UserActivityLog(String username, String permissions, Timestamp executionDate, String executedMethod){
        this.username = username;
        this.permissions = permissions;
        this.executionDate = executionDate;
        this.executedMethod = executedMethod;
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "permissions")
    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @Basic
    @Column(name = "executiondate")
    public Timestamp getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Timestamp executionDate) {
        this.executionDate = executionDate;
    }

    @Basic
    @Column(name = "executedmethod")
    public String getExecutedMethod() {
        return executedMethod;
    }

    public void setExecutedMethod(String executedMethod) {
        this.executedMethod = executedMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserActivityLog that = (UserActivityLog) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(username, that.username)) return false;
        if (!Objects.equals(permissions, that.permissions)) return false;
        if (!Objects.equals(executionDate, that.executionDate))
            return false;
        return Objects.equals(executedMethod, that.executedMethod);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        result = 31 * result + (executionDate != null ? executionDate.hashCode() : 0);
        result = 31 * result + (executedMethod != null ? executedMethod.hashCode() : 0);
        return result;
    }
}
