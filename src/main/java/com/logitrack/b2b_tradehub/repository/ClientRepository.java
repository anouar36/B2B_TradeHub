package com.logitrack.b2b_tradehub.repository;

import com.logitrack.b2b_tradehub.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    // Fixed the ID type issue here (Long, not String)
    Optional<Client> findByUserId(Long userId);
}