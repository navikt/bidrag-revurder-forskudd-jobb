package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api

import java.math.BigDecimal

data class HentSkattegrunnlagspostResponse(

  val skattegrunnlagType: String,
  val inntektType: String,
  val belop: BigDecimal
)
