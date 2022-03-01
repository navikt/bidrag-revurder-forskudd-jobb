package no.nav.bidrag.revurder.forskudd.jobb.enums

enum class BeregnForskuddGrunnlagType(val typeNavn: String, val rolle: Rolle, val flere: Boolean, val dato: Boolean) {
  GENERELL_INFO("GenerellInfo", Rolle.SOKNADSBARN, false, false),
  BOSTATUS("Bostatus", Rolle.SOKNADSBARN, false, true),
  INNTEKT("Inntekt", Rolle.BIDRAGSMOTTAKER, true, true),
  SIVILSTAND("Sivilstand", Rolle.BIDRAGSMOTTAKER, false, true),
  BARN_I_HUSSTAND("BarnIHusstand", Rolle.BIDRAGSMOTTAKER, false, true)
}

enum class Rolle(val rolleKode: String) {
  SOKNADSBARN("SB"),
  BIDRAGSMOTTAKER("BM")
}

enum class SivilstandKode {
  GIFT,
  ENSLIG,
  SAMBOER
}

enum class BostatusKode {
  ALENE,
  MED_FORELDRE,
  MED_ANDRE_ENN_FORELDRE
}

enum class InntektType(val beskrivelse: String, val belopstype: String) {
  INNTEKTSOPPLYSNINGER_ARBEIDSGIVER("Inntektsopplysninger fra arbeidsgiver", "AG"),
  SKATTEGRUNNLAG_SKE("Skattegrunnlag fra Skatteetaten", "LIGS")
}

enum class InntektTypeFraAinntekt {
  LOENNSINNTEKT
}

enum class SkattegrunnlagTypeFraSigrun {
  ORDINAER,
  SVALBARD
}

enum class InntektKategori {
  AINNTEKT,
  SIGRUN
}
