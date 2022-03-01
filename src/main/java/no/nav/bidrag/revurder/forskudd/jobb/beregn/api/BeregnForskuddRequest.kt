package no.nav.bidrag.revurder.forskudd.jobb.beregn.api

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

data class BeregnForskuddGrunnlag(
  val beregnDatoFra: LocalDate? = null,
  val beregnDatoTil: LocalDate? = null,
  val grunnlagListe: List<Grunnlag>? = null
)

data class Grunnlag(
  val referanse: String? = null,
  val type: String? = null,
  val innhold: JsonNode? = null
)
