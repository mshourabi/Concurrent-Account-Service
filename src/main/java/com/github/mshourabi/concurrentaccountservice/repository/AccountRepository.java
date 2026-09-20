package com.github.mshourabi.concurrentaccountservice.repository;

import com.github.mshourabi.concurrentaccountservice.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}
