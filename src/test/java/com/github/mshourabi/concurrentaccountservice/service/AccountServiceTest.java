package com.github.mshourabi.concurrentaccountservice.service;


import static org.junit.jupiter.api.Assertions.*;
import com.github.mshourabi.concurrentaccountservice.model.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Test
    public void testSaveAccount() {
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account("A", 100L));
        accounts.add(new Account("B", 200L));

        for (Account account : accounts) {
            accountService.save(account);
        }

        Account accountB = accountService.findAccountById("B");
        assertEquals(200L, accountB.getBalance());
    }
}