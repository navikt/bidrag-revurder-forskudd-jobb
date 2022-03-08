package no.nav.bidrag.revurder.forskudd.jobb.domene

import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori
import java.time.LocalDate

data class JobbParameter(
  val sisteMuligeDatoForSisteVedtak: LocalDate,
  val virkningsdato: LocalDate,
  val inntektKategori: InntektKategori,
  val filLokasjon: String
)
