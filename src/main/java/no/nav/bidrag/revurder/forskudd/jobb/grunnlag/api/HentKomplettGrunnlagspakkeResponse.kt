package no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api

data class HentKomplettGrunnlagspakkeResponse(

  val grunnlagspakkeId: Int = 0,
  val ainntektListe: List<HentAinntektResponse> = emptyList(),
  val skattegrunnlagListe: List<HentSkattegrunnlagResponse> = emptyList(),
  val persondata: HentPersondataResponse? = null
)
