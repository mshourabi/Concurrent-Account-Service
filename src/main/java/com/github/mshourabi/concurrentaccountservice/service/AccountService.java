package com.github.mshourabi.concurrentaccountservice.service;

import com.github.mshourabi.concurrentaccountservice.model.entity.Account;
import com.github.mshourabi.concurrentaccountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }


    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public Account findAccountById(String id) {
        return repository.findById(id).orElseThrow(()-> new RuntimeException("Account not found"));
    }

    @Transactional(rollbackFor = Throwable.class)
    public Account save(Account account) {
        return repository.save(account);
    }
}
