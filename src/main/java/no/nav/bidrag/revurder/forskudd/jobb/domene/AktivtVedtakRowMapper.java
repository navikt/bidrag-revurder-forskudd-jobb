package no.nav.bidrag.revurder.forskudd.jobb.domene;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class AktivtVedtakRowMapper implements RowMapper<AktivtVedtak> {
  @Override
  public AktivtVedtak mapRow(ResultSet resultSet, int i) throws SQLException {
    return new AktivtVedtak(
        resultSet.getInt("aktivt_vedtak_id"),
        resultSet.getInt("vedtak_id"),
        resultSet.getString("sak_id"),
        resultSet.getString("soknadsbarn_id"),
        resultSet.getString("mottaker_id"),
        resultSet.getDate("vedtak_dato_siste_vedtak").toLocalDate(),
        resultSet.getDate("vedtak_dato_siste_manuelle_vedtak").toLocalDate(),
        resultSet.getString("vedtak_type"),
        resultSet.getBigDecimal("belop"),
        resultSet.getString("valutakode"),
        resultSet.getString("resultatkode"),
        resultSet.getString("mottaker_sivilstand_siste_manuelle_vedtak"),
        resultSet.getInt("mottaker_antall_barn_siste_manuelle_vedtak"),
        resultSet.getString("soknadsbarn_bostedsstatus"),
        resultSet.getDate("soknadsbarn_fodselsdato").toLocalDate(),
        resultSet.getBoolean("soknadsbarn_har_unntakskode"),
        resultSet.getString("opprettet_av"),
        resultSet.getTimestamp("opprettet_timestamp").toLocalDateTime()
    );
  }

}
