package com.a18.auth.service;

import com.a18.auth.model.User;
import com.a18.auth.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractUserDetailsService<User, Long, UserRepository> {

  @Autowired private UserRepository userRepository;

  @Override public UserRepository getRepository() {
    return userRepository;
  }

  public Page<User> findBySpec(Specification<User> spec, Pageable pageable) {
    return this.userRepository.findAll(spec, pageable);
  }
}
