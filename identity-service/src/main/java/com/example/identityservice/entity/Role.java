package com.example.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("users") //避免互相引用!! 因為他們都持有對方 + M2M
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;


    @ManyToMany(mappedBy = "roles" )
    //@JsonManagedReference 搭配對面 JsonBackReference 避免無限找對方
    private List<UserCredential> users;
}
