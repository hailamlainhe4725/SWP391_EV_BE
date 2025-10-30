package com.example.demo.entity;

import com.example.demo.enums.ContractStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.example.demo.enums.UserRole;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    Long id;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Column(nullable = false, unique = true)
    String email;

    @Column
    String phone;

    @Column(nullable = false)
    String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserRole role = UserRole.USER;

    @Column(name = "deleted")
    Boolean deleted = false; // soft delete flag


    @Column(name = "cccd")
    String cccd;

    @Column(name = "gplx")
    String gplx;

}
