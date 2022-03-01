package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api

import java.math.BigDecimal
import java.time.LocalDate

data class HentAinntektspostResponse (

  val utbetalingsperiode: String? = "",
  val opptjeningsperiodeFra: LocalDate? = LocalDate.now(),
  val opptjeningsperiodeTil: LocalDate? = LocalDate.now(),
  val opplysningspliktigId: String? = "",
  val virksomhetId: String? = "",
  val inntektType: String = "",
  val fordelType: String? = "",
  val beskrivelse: String? = "",
  val belop: BigDecimal = BigDecimal.ZERO,
  val etterbetalingsperiodeFra: LocalDate? = LocalDate.now(),
  val etterbetalingsperiodeTil: LocalDate? = LocalDate.now()
)
