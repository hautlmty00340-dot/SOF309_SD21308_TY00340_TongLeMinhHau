package com.poly.sof309.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(name = "uk_account_contact", columnNames = "contact"))
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String fullName;
    @Column(nullable = false, length = 254)
    private String contact;
    @Column(nullable = false, length = 100)
    private String passwordHash;

    protected Account() { }
    public Account(String fullName, String contact, String passwordHash) {
        this.fullName = fullName;
        this.contact = contact;
        this.passwordHash = passwordHash;
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getContact() { return contact; }
    public String getPasswordHash() { return passwordHash; }
}
