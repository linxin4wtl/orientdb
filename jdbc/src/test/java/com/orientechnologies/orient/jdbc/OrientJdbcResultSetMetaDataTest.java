package com.orientechnologies.orient.jdbc;

import com.orientechnologies.orient.core.id.ORecordId;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static java.sql.Types.BIGINT;
import static org.assertj.core.api.Assertions.assertThat;

public class OrientJdbcResultSetMetaDataTest extends OrientJdbcBaseTest {

  @Test
  public void shouldMapOrientTypesToJavaSQLTypes() throws Exception {

    ResultSet rs = conn.createStatement().executeQuery("SELECT stringKey, intKey, text, length, date, score FROM Item");

    ResultSetMetaData metaData = rs.getMetaData();
    assertThat(metaData).isNotNull();
    assertThat(metaData.getColumnCount()).isEqualTo(6);

    assertThat(metaData.getColumnType(1)).isEqualTo(Types.VARCHAR);
    assertThat(metaData.getColumnClassName(1)).isEqualTo(String.class.getName());

    assertThat(metaData.getColumnType(2)).isEqualTo(Types.INTEGER);

    assertThat(metaData.getColumnType(3)).isEqualTo(Types.VARCHAR);
    assertThat(rs.getObject(3)).isInstanceOf(String.class);

    assertThat(metaData.getColumnType(4)).isEqualTo(BIGINT);
    assertThat(metaData.getColumnType(5)).isEqualTo(Types.TIMESTAMP);

    assertThat(metaData.getColumnType(6)).isEqualTo(Types.DECIMAL);
  }

  @Test
  public void shouldMapReturnTypes() throws Exception {

    assertThat(conn.isClosed()).isFalse();

    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT stringKey, intKey, text, length, date, score FROM Item");

    assertThat(rs.getString(1)).isEqualTo("1");
    assertThat(rs.getString("stringKey")).isEqualTo("1");
    assertThat(rs.findColumn("stringKey")).isEqualTo(1);

    assertThat(rs.getInt(2)).isEqualTo(1);
    assertThat(rs.getInt("intKey")).isEqualTo(1);

    assertThat(rs.getString("text")).hasSize(rs.getInt("length"));

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.add(Calendar.HOUR_OF_DAY, -1);
    Date date = new Date(cal.getTimeInMillis());
    assertThat(rs.getDate("date").toString()).isEqualTo(date.toString());
    assertThat(rs.getDate(5).toString()).isEqualTo(date.toString());

    //DECIMAL
    assertThat(rs.getBigDecimal("score")).isEqualTo(BigDecimal.valueOf(959));

  }

  @Test
  public void shouldMapRatingToDouble() throws Exception {

    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Author limit 10");
    int size = 0;
    while (rs.next()) {
      assertThat(rs.getDouble("rating")).isNotNull().isInstanceOf(Double.class);

      size++;
    }
    assertThat(size).isEqualTo(10);
  }

  @Test
  public void shouldConvertUUIDToDouble() throws Exception {

    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Author limit 10");
    int count = 0;
    while (rs.next()) {
      assertThat(rs.getDouble("uuid")).isNotNull().isInstanceOf(Double.class);
      count++;
    }
    assertThat(count).isEqualTo(10);
  }

  @Test
  public void shouldNavigateResultSetByMetadata() throws Exception {

    assertThat(conn.isClosed()).isFalse();
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT @rid, @class, stringKey, intKey, text, length, date FROM Item");

    rs.next();
    ResultSetMetaData metaData = rs.getMetaData();
    assertThat(metaData.getColumnCount()).isEqualTo(7);

    assertThat(metaData.getColumnName(1)).isEqualTo("@rid");
    assertThat(new ORecordId(rs.getString(1)).isPersistent()).isEqualTo(true);

    assertThat(rs.getObject(1)).isInstanceOf(String.class);

    assertThat(metaData.getColumnName(2)).isEqualTo("@class");
    assertThat(rs.getString(2)).isEqualTo("Item");
    assertThat(rs.getObject(2)).isInstanceOf(String.class);

    assertThat(metaData.getColumnName(3)).isEqualTo("stringKey");
    assertThat(rs.getObject(3)).isInstanceOf(String.class);

    assertThat(metaData.getColumnName(4)).isEqualTo("intKey");

    assertThat(metaData.getColumnName(5)).isEqualTo("text");
    assertThat(rs.getObject(5)).isInstanceOf(String.class);

    assertThat(metaData.getColumnName(6)).isEqualTo("length");

    assertThat(metaData.getColumnName(7)).isEqualTo("date");

  }

  @Test
  public void shouldMapMissingFieldsToNull() throws Exception {
    Statement stmt = conn.createStatement();

    ResultSet rs = stmt.executeQuery(
        "select uuid, posts.* as post_ from (\n" + " select uuid, out('Writes') as posts from writer  unwind posts) order by uuid");

    while (rs.next()) {
      int columnCount = rs.getMetaData().getColumnCount();

      assertThat(columnCount).isEqualTo(6);

      //map a record to a Map
      Map<String, Object> record = new HashMap<String, Object>();
      for (int i = 1; i <= columnCount; i++) {
        record.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
      }

      //all fields must be present even if with  null values
      assertThat(record).containsKeys("uuid", "post_date", "post_uuid", "post_title", "post_content", "post_in_Writes");

    }

  }

  @Test
  public void shouldMapMissingFieldsToNull2() throws Exception {
    Statement stmt = conn.createStatement();

    ResultSet rs = stmt.executeQuery(
        "select writer_uuid, posts.uuid as post_uuid,  posts.date as post_date, posts.title as post_title  from (\n"
            + " select uuid as writer_uuid, out('Writes') as posts from writer  unwind posts) order by writer_uuid");

    while (rs.next()) {

      int columnCount = rs.getMetaData().getColumnCount();

      assertThat(columnCount).isEqualTo(4);

      //map a record to a Map
      Map<String, Object> record = new HashMap<String, Object>();
      for (int i = 1; i <= columnCount; i++) {
        record.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
      }

      //all fields must be present even if with  null values
      assertThat(record).containsKeys("writer_uuid", "post_uuid", "post_date", "post_title");

    }
  }

  @Test
  public void shouldFetchMetadataTheSparkStyle() throws Exception {

    //set spark "profile"

    conn.getInfo().setProperty("spark", "true");
    Statement stmt = conn.createStatement();

    ResultSet rs = stmt.executeQuery("select * from (select * from item) WHERE 1=0");

    ResultSetMetaData metaData = rs.getMetaData();

    assertThat(metaData.getColumnName(1)).isEqualTo("stringKey");
    assertThat(metaData.getColumnTypeName(1)).isEqualTo("STRING");
    assertThat(rs.getObject(1)).isInstanceOf(String.class);

  }

  @Test
  public void shouldReadBoolean() throws Exception {

    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT  isActive, is_active FROM Writer");

    while (rs.next()) {
      assertThat(rs.getBoolean(1)).isTrue();
      assertThat(rs.getBoolean(2)).isTrue();

    }
  }
}