package com.github.mshourabi.concurrentaccountservice.model.entity;

import jakarta.persistence.*;

@Table(name = "TBL_ACCOUNT")
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long balance;

    @Version
    private Long version;

    public Account() {
    }

    public Account(int id, Long balance, Long version) {
        this.id = id;
        this.balance = balance;
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
