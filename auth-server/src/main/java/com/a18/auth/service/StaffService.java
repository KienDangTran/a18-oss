package com.a18.auth.service;

import com.a18.auth.model.Staff;
import com.a18.auth.model.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffService extends AbstractUserDetailsService<Staff, Long, StaffRepository> {

  @Autowired private StaffRepository staffRepository;

  @Override public StaffRepository getRepository() {
    return this.staffRepository;
  }
}
