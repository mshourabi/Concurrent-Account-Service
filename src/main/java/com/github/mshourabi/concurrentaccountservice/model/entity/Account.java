package com.github.mshourabi.concurrentaccountservice.model.entity;

import jakarta.persistence.*;

@Table(name = "TBL_ACCOUNT")
@Entity
public class Account {

    @Id
    private String id;

    @Column(nullable = false)
    private long balance;

    @Version
    private long version;

    public Account() {
    }

    public Account(String id, Long balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

}
