package com.dev_high.common.util;

import com.dev_high.common.annotation.CustomGeneratedId;
import java.lang.reflect.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;

public class CustomIdGenerator implements BeforeExecutionGenerator {

  private final String tableNm;


  public CustomIdGenerator(CustomGeneratedId customGeneratedId, Member idMember,
      CustomIdGeneratorCreationContext creationContext) {
    this.tableNm = customGeneratedId.method(); // 엔티티에서 전달한 테이블명
  }

  @Override
  public EnumSet<EventType> getEventTypes() {
    return EnumSet.of(EventType.INSERT);
  }

  @Override
  public Object generate(SharedSessionContractImplementor session, Object owner,
      Object currentValue, EventType eventType) {

    if (isH2(session)) {
      return generateIdForH2(tableNm);
    } else if (isPostgres(session)) {
      return callPostgresFunction(tableNm, session);
    } else {
      throw new HibernateException("Unsupported DB dialect");
    }
  }

  // ---------------- H2 테스트용 ----------------
  private static final AtomicLong H2_SEQ = new AtomicLong(0);

  private String generateIdForH2(String tableNm) {
    long seq = H2_SEQ.incrementAndGet();
    String prefix = tableNm.length() >= 3 ? tableNm.substring(0, 3) : tableNm;
    return prefix.toUpperCase() + String.format("%08d", seq);
  }

  private String callPostgresFunction(String tableNm, SharedSessionContractImplementor session) {
    String newId = null;
    String sql = "SELECT fn_nextval('" + tableNm + "')";

    try (Connection con = session.getJdbcConnectionAccess().obtainConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      if (rs.next()) {
        newId = rs.getString(1);
      }
    } catch (SQLException e) {
      throw new HibernateException(e);
    }

    return newId;
  }


  private boolean isH2(SharedSessionContractImplementor session) {
    String dialect = session.getJdbcServices().getDialect().toString().toLowerCase();
    return dialect.contains("h2");
  }

  private boolean isPostgres(SharedSessionContractImplementor session) {
    String dialect = session.getJdbcServices().getDialect().toString().toLowerCase();
    return dialect.contains("postgres");
  }
}

