package no.nav.bidrag.revurder.forskudd.jobb.beregn.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.time.LocalDate

data class BeregnetForskuddResultat(
  val beregnetForskuddPeriodeListe: List<ResultatPeriode> = emptyList(),
  val grunnlagListe: List<ResultatGrunnlag> = emptyList()
)

data class ResultatPeriode(
  val periode: Periode = Periode(),
  val resultat: ResultatBeregning = ResultatBeregning(),
  val grunnlagReferanseListe: List<String> = emptyList()
)

data class ResultatBeregning(
  val belop: BigDecimal = BigDecimal.ZERO,
  val kode: String = "",
  val regel: String = ""
)

data class ResultatGrunnlag(
  val referanse: String = "",
  val type: String = "",
  val innhold: JsonNode = ObjectMapper().createObjectNode()
)

data class Periode(
  val datoFom: LocalDate? = null,
  val datoTil: LocalDate? = null
)
