package com.dev_high.user.auth.infrastructure;

import com.dev_high.user.auth.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenCrudRepository extends CrudRepository<RefreshToken, String>{
}
