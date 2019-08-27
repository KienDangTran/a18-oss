package com.a18.auth.model;

import com.a18.common.dto.BaseEntity;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

@Data
@EqualsAndHashCode(of = {"cardNo", "type"})
@ToString(exclude = {"user"})
@Entity
@Table(name = "user_document", schema = "auth")
public class UserDocument extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User user;

  @Column(nullable = false, updatable = false)
  @ColumnTransformer(read = "UPPER(card_no)", write = "UPPER(?)")
  private String cardNo;

  @Column(nullable = false, updatable = false)
  @Enumerated(EnumType.STRING)
  private DocumentType type = DocumentType.ID_CARD;

  private LocalDate issueDate;

  private LocalDate expireDate;

  @Enumerated(EnumType.STRING)
  private DocumentStatus status;

  public enum DocumentType {
    ID_CARD, PASSPORT
  }

  public enum DocumentStatus {
    ACTIVE, SUSPENDED
  }
}
