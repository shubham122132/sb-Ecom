package com.ecommerce.project.model;

import jakarta.persistence.*;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Column(length = 20, name = "role_name")
    private AppRole roleName;

    public Role(AppRole roleName) {
        this.roleName = roleName;
    }
}
