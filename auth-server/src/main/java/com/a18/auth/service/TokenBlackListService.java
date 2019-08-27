package com.a18.auth.service;

import com.a18.auth.model.TokenBlacklist;
import com.a18.auth.model.repository.TokenBlackListRepository;
import java.util.Date;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class TokenBlackListService {

  @Autowired @Lazy TokenBlackListRepository tokenBlackListRepo;

  public Boolean isTokenExists(String jti) {
    return this.tokenBlackListRepo.findByJti(jti).isPresent();
  }

  public Boolean isBlacklisted(String jti) {
    Assert.isTrue(StringUtils.isNotBlank(jti), "cannot check isBlacklisted 'coz jti is blank");
    return this.tokenBlackListRepo.findByJti(jti).map(TokenBlacklist::getBlacklisted).orElse(false);
  }

  @Async
  @Transactional
  public void addToEnabledList(String username, String jti, Long expired) {
    Assert.isTrue(
        StringUtils.isNotBlank(username),
        "cannot addToEnabledList 'coz username is blank"
    );
    Assert.isTrue(StringUtils.isNotBlank(jti), "cannot addToEnabledList 'coz jti is blank");
    Assert.notNull(expired, "cannot addToEnabledList 'coz expired is null");

    // clean all black listed tokens for user
    this.tokenBlackListRepo.findAllByUsername(username).forEach(token -> {
      token.setBlacklisted(true);
      this.tokenBlackListRepo.save(token);
    });

    // Add new token white listed
    TokenBlacklist tokenBlacklist = new TokenBlacklist();
    tokenBlacklist.setJti(jti);
    tokenBlacklist.setUsername(username);
    tokenBlacklist.setExpiresIn(expired);
    tokenBlacklist.setBlacklisted(false);
    this.tokenBlackListRepo.save(tokenBlacklist);

    //delete all expired token
    this.tokenBlackListRepo.deleteAllByUsernameAndExpiresInBefore(username, new Date().getTime());
  }

  @Async
  @Transactional
  public void addToBlackList(String jti) {
    Assert.isTrue(StringUtils.isNotBlank(jti), "cannot addToBlackList 'coz jti is blank");

    Optional<TokenBlacklist> tokenBlackList = tokenBlackListRepo.findByJti(jti);
    if (tokenBlackList.isPresent()) {
      tokenBlackList.get().setBlacklisted(true);
      this.tokenBlackListRepo.save(tokenBlackList.get());
    } else {
      throw new EntityNotFoundException(String.format("Token with jti[%s] not found.", jti));
    }
  }

  @Async
  @Transactional
  public void addAllToBlacklistByUsername(String username) {
    Assert.isTrue(
        StringUtils.isNotBlank(username),
        "cannot addAllToBlacklistByUsername 'coz username is blank"
    );
    this.tokenBlackListRepo.findAllByUsername(username).forEach(token -> {
      token.setBlacklisted(true);
      this.tokenBlackListRepo.save(token);
    });
  }
}
