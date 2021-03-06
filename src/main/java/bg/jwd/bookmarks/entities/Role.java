package bg.jwd.bookmarks.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Roles")
public class Role implements Serializable {

	private static final long serialVersionUID = 4811121776182105848L;

	@Id
	@GenericGenerator(name = "rolesGen", strategy = "increment")
	@GeneratedValue(generator = "rolesGen")
	@Column(name = "role_id")
	private int roleId;

	@Column(name = "role_name")
	private String roleName;

	@ManyToMany(mappedBy = "roles", cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH })
	private Set<User> users;

	public Role() {
	}

	public Role(String roleName) {
		this.roleName = roleName;
		this.users = new HashSet<User>();
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Set<User> getUsers() {
		return this.users;
	}

	@Override
	public String toString() {
		return this.roleName;
	}
	
	@Override
    public int hashCode() {
        return this.roleName.hashCode();
    }

	// TODO: height quality override / null ??
	@Override
	public boolean equals(Object that) {
		Role other = (Role)that;
		return this.roleName.equals(other.roleName);
	}
}
