package no.nav.bidrag.revurder.forskudd.jobb.beregn.dto

data class GenerellInfo(
  val rolle: String,
  val fodselsdato: String
)

data class Bostatus(
  val rolle: String,
  val datoFom: String,
  val datoTil: String?,
  val bostatusKode: String
)

data class Inntekt(
  val rolle: String,
  val datoFom: String,
  val datoTil: String?,
  val inntektType: String,
  val belop: Integer
)

data class Sivilstand(
  val rolle: String,
  val datoFom: String,
  val datoTil: String?,
  val sivilstandKode: String
)

data class BarnIHusstand(
  val rolle: String,
  val datoFom: String,
  val datoTil: String?,
  val antall: Integer
)
