package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api

import java.time.LocalDate
import java.time.LocalDateTime

data class HentSkattegrunnlagResponse(

  val personId: String,
  val periodeFra: LocalDate,
  val periodeTil: LocalDate,
  val aktiv: Boolean,
  val brukFra: LocalDateTime,
  val brukTil: LocalDateTime?,
  val hentetTidspunkt: LocalDateTime,
  val skattegrunnlagListe: List<HentSkattegrunnlagspostResponse>
)
