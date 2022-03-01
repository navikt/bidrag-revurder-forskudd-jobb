package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api

import java.time.LocalDate
import java.time.LocalDateTime

data class HentAinntektResponse(

  val personId: String = "",
  val periodeFra: LocalDate = LocalDate.now(),
  val periodeTil: LocalDate = LocalDate.now(),
  val aktiv: Boolean = true,
  val brukFra: LocalDateTime = LocalDateTime.now(),
  val brukTil: LocalDateTime? = LocalDateTime.now(),
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),
  val ainntektspostListe: List<HentAinntektspostResponse> = emptyList()
  )
