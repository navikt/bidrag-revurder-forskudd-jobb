package no.nav.bidrag.revurder.forskudd.jobb.domene

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

data class AktivtVedtak(
  val aktivtVedtakId: Int,
  val vedtakId: Int,
  val sakId: String,
  val soknadsbarnId: String,
  val mottakerId: String,
  val vedtakDatoSisteVedtak: LocalDate,
  val vedtakDatoSisteManuelleVedtak: LocalDate,
  val vedtakType: String,
  val belop: BigDecimal,
  val valutakode: String,
  val resultatkode: String,
  val mottakerSivilstandSisteManuelleVedtak: String,
  val mottakerAntallBarnSisteManuelleVedtak: Int,
  val soknadsbarnBostedsstatus: String,
  val soknadsbarnFodselsdato: LocalDate,
  //TODO Sjekke om unntakskode må inneholde noe mer enn en boolean-verdi
  val soknadsbarnHarUnntakskode: Boolean,
  val opprettetAv: String,
  val opprettetTimestamp: LocalDateTime
) {

  // Validerer om det aktive vedtaket kvalifiserer for revurdering. Følgende kriterier må være oppfylt:
  // - Søknadsbarnet er under 18 år
  // - Dato for siste manuelle vedtak må ikke være etter siste aktuelle dato (parameterverdi)
  // - Søknadsbarnet kan ikke ha unntakskoder
  fun kvalifisererForRevurdering(sisteMuligeDatoForSisteVedtak: LocalDate, virkningsdato: LocalDate): Boolean {
    return (soknadsbarnetErUnder18Aar(virkningsdato) && sisteManuelleVedtakErGammeltNok(sisteMuligeDatoForSisteVedtak) &&
        (!soknadsbarnHarUnntakskode))
  }

  private fun soknadsbarnetErUnder18Aar(virkningsdato: LocalDate): Boolean {
    return Period.between(soknadsbarnFodselsdato, virkningsdato).getYears() < 18
  }

  private fun sisteManuelleVedtakErGammeltNok(sisteMuligeDatoForSisteVedtak: LocalDate): Boolean {
    return (!(vedtakDatoSisteManuelleVedtak.isAfter(sisteMuligeDatoForSisteVedtak)))
  }
}