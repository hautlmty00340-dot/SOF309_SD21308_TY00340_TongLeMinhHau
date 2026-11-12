package com.poly.sof309.repository;

import com.poly.sof309.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByContactIgnoreCase(String contact);
    boolean existsByContactIgnoreCase(String contact);
}
