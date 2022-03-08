package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api

import java.time.LocalDate
import java.time.LocalDateTime

data class HentPersondataResponse(

  val personIdMottaker: String = "",
  val personIdSoknadsbarn: String = "",
  val periodeFra: LocalDate = LocalDate.now(),
  val periodeTil: LocalDate = LocalDate.now(),
  val aktiv: Boolean = true,
  val brukFra: LocalDateTime = LocalDateTime.now(),
  val brukTil: LocalDateTime? = LocalDateTime.now(),
  val mottakerSivilstand: String = "",
  val mottakerAntallBarnIHusstand: Int = 0,
  val soknadsbarnBostatus: String = "",
  val soknadsbarnFodselsdato: LocalDate = LocalDate.now(),
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
)
