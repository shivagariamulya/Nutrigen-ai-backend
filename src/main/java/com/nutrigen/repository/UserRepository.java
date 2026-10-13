package com.nutrigen.repository;
import com.nutrigen.model.UserProfile; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface UserRepository extends JpaRepository<UserProfile,Long>{Optional<UserProfile> findByEmail(String email);}
