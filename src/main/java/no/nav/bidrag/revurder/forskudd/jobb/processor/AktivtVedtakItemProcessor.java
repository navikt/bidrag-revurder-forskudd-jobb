package no.nav.bidrag.revurder.forskudd.jobb.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.api.BeregnForskuddGrunnlag;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.api.BeregnetForskuddResultat;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.api.Grunnlag;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.dto.BarnIHusstand;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.dto.Bostatus;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.dto.GenerellInfo;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.dto.Inntekt;
import no.nav.bidrag.revurder.forskudd.jobb.beregn.dto.Sivilstand;
import no.nav.bidrag.revurder.forskudd.jobb.consumer.beregn.BeregnConsumer;
import no.nav.bidrag.revurder.forskudd.jobb.consumer.grunnlag.GrunnlagConsumer;
import no.nav.bidrag.revurder.forskudd.jobb.domene.AktivtVedtak;
import no.nav.bidrag.revurder.forskudd.jobb.enums.BeregnForskuddGrunnlagType;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektKategori;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektType;
import no.nav.bidrag.revurder.forskudd.jobb.enums.InntektTypeFraAinntekt;
import no.nav.bidrag.revurder.forskudd.jobb.enums.SivilstandKode;
import no.nav.bidrag.revurder.forskudd.jobb.enums.SkattegrunnlagTypeFraSigrun;
import no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api.HentAinntektResponse;
import no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api.HentKomplettGrunnlagspakkeResponse;
import no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api.HentPersondataResponse;
import no.nav.bidrag.revurder.forskudd.jobb.grunnlag.api.HentSkattegrunnlagResponse;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

public class AktivtVedtakItemProcessor implements ItemProcessor<AktivtVedtak, String> {

  @Value("#{jobParameters['sisteMuligeDatoForSisteVedtak']}")
  private String sisteMuligeDatoForSisteVedtak;

  @Value("#{jobParameters['virkningsdato']}")
  private String virkningsdato;

  @Value("#{jobParameters['inntektKategori']}")
  private String inntektKategori;

  private final GrunnlagConsumer grunnlagConsumer;
  private final BeregnConsumer beregnConsumer;

  public AktivtVedtakItemProcessor(GrunnlagConsumer grunnlagConsumer, BeregnConsumer beregnConsumer) {
    this.grunnlagConsumer = grunnlagConsumer;
    this.beregnConsumer = beregnConsumer;
  }

  @Override
  public String process(AktivtVedtak aktivtVedtak) {

    if (!(aktivtVedtak.kvalifisererForRevurdering(LocalDate.parse(sisteMuligeDatoForSisteVedtak), LocalDate.parse(virkningsdato)))) {
      System.out.println("Vedtak " + aktivtVedtak.getVedtakId() + " kvalifiserer ikke for behandling");
      return null;
    }

    System.out.println("Vedtak " + aktivtVedtak.getVedtakId() + " kvalifiserer for behandling");

//    if (aktivtVedtak.getVedtakId() > 6) {
//      System.out.println("Kaster exception");
//      throw new RuntimeException("Programmert exception");
//    }

    // Henter grunnlag for en gitt grunnlagspakke
    //TODO Her m?? det egentlig kj??res 3 kall mot bidrag-grunnlag (opprett/oppdater/hent) - b??r vurdere et nytt endepunkt i bidrag-grunnlag for ??
    //TODO kunne gj??re alt i ett kall
    var hentKomplettGrunnlagspakkeResponse = Optional.ofNullable(grunnlagConsumer.hentGrunnlagspakke(1).getResponseEntity().getBody()).orElse(null);
    System.out.println("Hentet f??lgende grunnlagspakke: " + hentKomplettGrunnlagspakkeResponse);

    // Filtrerer bort vedtak som ikke skal revurderes basert p?? grunnlagsdata
    if (vedtakSkalFiltreresBortBasertPaaGrunnlag(aktivtVedtak, hentKomplettGrunnlagspakkeResponse, InntektKategori.valueOf(inntektKategori))) {
      return null;
    }

    // Bygger opp request til kall av beregning
    //TODO H??ndtere at hentKomplettGrunnlagspakkeResponse er null
    var beregnForskuddRequest = byggBeregnForskuddGrunnlag(hentKomplettGrunnlagspakkeResponse, aktivtVedtak);
    System.out.println("Kaller beregn forskudd med f??lgende grunnlag: " + beregnForskuddRequest);

    // Beregner forskudd
    var beregnForskuddResponse = Optional.ofNullable(beregnConsumer.beregnForskudd(beregnForskuddRequest).getResponseEntity().getBody()).orElse(null);
    System.out.println("Mottatt f??lgende respons fra beregn forskudd: " + beregnForskuddResponse);

    // Sammenligner resultatet av beregningen med l??pende forskudd og lager et forslag til nytt vedtak
    var forslagTilNyttVedtak = lagForslagTilNyttVedtak(aktivtVedtak, beregnForskuddResponse);

    return forslagTilNyttVedtak;
  }

  // Hvis det finnes AAP-inntekt og antall barn i husstand er mer enn 1 skal det ikke fattes nytt vedtak
  //TODO Sjekke om denne regelen stemmer
  private boolean vedtakSkalFiltreresBortBasertPaaGrunnlag(AktivtVedtak aktivtVedtak,
      HentKomplettGrunnlagspakkeResponse hentKomplettGrunnlagspakkeResponse, InntektKategori inntektKategori) {
    //TODO Implementere logikk
    //For A-inntekt ligger informasjon om AAP under:
    // inntektType = 'Ytelse fra offentlig'
    // fordelType = 'Kontantytelse'
    // beskrivelse = 'Arbeidsavklaringspenger'
    //For Sigrun ligger informasjon om AAP under:
    // inntektType = 'arbeidsavklaringspenger'
    return false;
  }

  private BeregnForskuddGrunnlag byggBeregnForskuddGrunnlag(HentKomplettGrunnlagspakkeResponse hentKomplettGrunnlagspakkeResponse,
      AktivtVedtak aktivtVedtak) {
    var grunnlagListe = new ArrayList<Grunnlag>();
    grunnlagListe.add(byggGenerellInfoGrunnlag(hentKomplettGrunnlagspakkeResponse.getPersondata(), LocalDate.parse(virkningsdato)));
    grunnlagListe.add(byggBostatusGrunnlag(hentKomplettGrunnlagspakkeResponse.getPersondata(), LocalDate.parse(virkningsdato)));
    //TODO Inntekter m?? gjennomg??s med tanke p?? mapping, filtrering og summering
    switch (InntektKategori.valueOf(inntektKategori)) {
      case AINNTEKT -> grunnlagListe.addAll(
          byggInntektGrunnlagAinntekt(hentKomplettGrunnlagspakkeResponse.getAinntektListe(), LocalDate.parse(virkningsdato)));
      case SIGRUN -> grunnlagListe.addAll(
          byggInntektGrunnlagSigrun(hentKomplettGrunnlagspakkeResponse.getSkattegrunnlagListe(), LocalDate.parse(virkningsdato)));
    }
    grunnlagListe.add(byggSivilstandGrunnlag(hentKomplettGrunnlagspakkeResponse.getPersondata(), aktivtVedtak, LocalDate.parse(virkningsdato)));
    grunnlagListe.add(byggBarnIHusstandGrunnlag(hentKomplettGrunnlagspakkeResponse.getPersondata(), LocalDate.parse(virkningsdato)));

    return new BeregnForskuddGrunnlag(LocalDate.parse(virkningsdato), null, grunnlagListe);
  }

  private Grunnlag byggGenerellInfoGrunnlag(HentPersondataResponse persondata, LocalDate virkningsdato) {
    var referanse =
        "Mottatt_" + BeregnForskuddGrunnlagType.GENERELL_INFO.getTypeNavn() + sjekkDatoRef(BeregnForskuddGrunnlagType.GENERELL_INFO, virkningsdato);
    var type = BeregnForskuddGrunnlagType.GENERELL_INFO.getTypeNavn();
    var rolle = BeregnForskuddGrunnlagType.GENERELL_INFO.getRolle().getRolleKode();
    //TODO F??dselsdato kan alternativt hentes fra aktivt_vedtak
    var fodselsdato = persondata.getSoknadsbarnFodselsdato().toString();
    var mapper = new ObjectMapper();
    return new Grunnlag(referanse, type, mapper.valueToTree(new GenerellInfo(rolle, fodselsdato)));
  }

  private Grunnlag byggBostatusGrunnlag(HentPersondataResponse persondata, LocalDate virkningsdato) {
    var referanse = "Mottatt_" + BeregnForskuddGrunnlagType.BOSTATUS.getTypeNavn() + sjekkDatoRef(BeregnForskuddGrunnlagType.BOSTATUS, virkningsdato);
    var type = BeregnForskuddGrunnlagType.BOSTATUS.getTypeNavn();
    var rolle = BeregnForskuddGrunnlagType.BOSTATUS.getRolle().getRolleKode();
    var datoFom = virkningsdato.toString();
    var bostatusKode = persondata.getSoknadsbarnBostatus();
    var mapper = new ObjectMapper();
    return new Grunnlag(referanse, type, mapper.valueToTree(new Bostatus(rolle, datoFom, null, bostatusKode)));
  }

  private List<Grunnlag> byggInntektGrunnlagAinntekt(List<HentAinntektResponse> inntektListe, LocalDate virkningsdato) {
    var grunnlagListe = new ArrayList<Grunnlag>();
    inntektListe.forEach(
        inntekt -> inntekt.getAinntektspostListe().forEach(
            inntektspost -> {
              //TODO Her m?? det legges inn mer sofistikert logikk
              if (inntektspost.getInntektType().equals(InntektTypeFraAinntekt.LOENNSINNTEKT.toString())) {
                var referanse = "Mottatt_" + BeregnForskuddGrunnlagType.INNTEKT.getTypeNavn() + "_" +
                    InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER.getBelopstype() + sjekkDatoRef(BeregnForskuddGrunnlagType.INNTEKT, virkningsdato);
                var type = BeregnForskuddGrunnlagType.INNTEKT.getTypeNavn();
                var rolle = BeregnForskuddGrunnlagType.INNTEKT.getRolle().getRolleKode();
                var datoFom = virkningsdato.toString();
                var inntektType = InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER.toString();
                var belop = inntektspost.getBelop().intValueExact();
                var mapper = new ObjectMapper();
                grunnlagListe.add(new Grunnlag(referanse, type, mapper.valueToTree(new Inntekt(rolle, datoFom, null, inntektType, belop))));
              }
            }
        )
    );
    return grunnlagListe;
  }

  private List<Grunnlag> byggInntektGrunnlagSigrun(List<HentSkattegrunnlagResponse> inntektListe, LocalDate virkningsdato) {
    var grunnlagListe = new ArrayList<Grunnlag>();
    inntektListe.forEach(
        inntekt -> inntekt.getSkattegrunnlagListe().forEach(
            inntektspost -> {
              //TODO Her m?? det legges inn mer sofistikert logikk
              if (inntektspost.getSkattegrunnlagType().equals(SkattegrunnlagTypeFraSigrun.ORDINAER.toString())) {
                var referanse = "Mottatt_" + BeregnForskuddGrunnlagType.INNTEKT.getTypeNavn() + "_" +
                    InntektType.SKATTEGRUNNLAG_SKE.getBelopstype() + sjekkDatoRef(BeregnForskuddGrunnlagType.INNTEKT, virkningsdato);
                var type = BeregnForskuddGrunnlagType.INNTEKT.getTypeNavn();
                var rolle = BeregnForskuddGrunnlagType.INNTEKT.getRolle().getRolleKode();
                var datoFom = virkningsdato.toString();
                var inntektType = InntektType.SKATTEGRUNNLAG_SKE.toString();
                var belop = inntektspost.getBelop().intValueExact();
                var mapper = new ObjectMapper();
                grunnlagListe.add(new Grunnlag(referanse, type, mapper.valueToTree(new Inntekt(rolle, datoFom, null, inntektType, belop))));
              }
            }
        )
    );
    return grunnlagListe;
  }

  private Grunnlag byggSivilstandGrunnlag(HentPersondataResponse persondata, AktivtVedtak aktivtVedtak, LocalDate virkningsdato) {
    var referanse =
        "Mottatt_" + BeregnForskuddGrunnlagType.SIVILSTAND.getTypeNavn() + sjekkDatoRef(BeregnForskuddGrunnlagType.SIVILSTAND, virkningsdato);
    var type = BeregnForskuddGrunnlagType.SIVILSTAND.getTypeNavn();
    var rolle = BeregnForskuddGrunnlagType.SIVILSTAND.getRolle().getRolleKode();
    var datoFom = virkningsdato.toString();
    var sivilstandKode = settSivilstandkode(persondata, aktivtVedtak);
    var mapper = new ObjectMapper();
    return new Grunnlag(referanse, type, mapper.valueToTree(new Sivilstand(rolle, datoFom, null, sivilstandKode)));
  }

  //TODO Verfifisere regelverk for bruk av sivilstand
  //Regelverk for ?? sette sivilstand:
  // - hvis dato for siste manuelle vedtak er nyere enn dato for siste oppdatering av sivilstand i PDL, brukes sivilstand fra siste manuelle vedtak
  // - ellers brukes sivilstand fra PDL
  // - unntak: hvis sivilstand fra siste manuelle vedtak er SAMBOER og sivilstand fra PDL er noe annet enn GIFT, settes sivilstand til SAMBOER
  private String settSivilstandkode(HentPersondataResponse persondata, AktivtVedtak aktivtVedtak) {
    if (aktivtVedtak.getMottakerSivilstandSisteManuelleVedtak().equals(SivilstandKode.SAMBOER.toString()) &&
        (!(persondata.getMottakerSivilstand().equals(SivilstandKode.GIFT.toString())))) {
      return aktivtVedtak.getMottakerSivilstandSisteManuelleVedtak();
    }
    //TODO Her er det egentlig dato for n??r sivilstand sist ble oppdatert i PDL som skal benyttes, ikke n??r det sist ble hentet (som vil v??re N??)
    if (aktivtVedtak.getVedtakDatoSisteManuelleVedtak().isAfter(persondata.getHentetTidspunkt().toLocalDate())) {
      return aktivtVedtak.getMottakerSivilstandSisteManuelleVedtak();
    }
    return persondata.getMottakerSivilstand();
  }

  //TODO M?? endre i bidrag-beregn-forskudd-rest, slik at den tar type 'BarnIHusstand' som inoput type i stedet for 'Barn' (blir da tilsvarende som
  //TODO i bidrag-beregn-barnebidrag-rest
  private Grunnlag byggBarnIHusstandGrunnlag(HentPersondataResponse persondata, LocalDate virkningsdato) {
    var referanse = "Mottatt_" + BeregnForskuddGrunnlagType.BARN_I_HUSSTAND.getTypeNavn() + sjekkDatoRef(BeregnForskuddGrunnlagType.BARN_I_HUSSTAND,
        virkningsdato);
    var type = BeregnForskuddGrunnlagType.BARN_I_HUSSTAND.getTypeNavn();
    var rolle = BeregnForskuddGrunnlagType.BARN_I_HUSSTAND.getRolle().getRolleKode();
    var datoFom = virkningsdato.toString();
    //TODO Avklare om antall barn skal hentes fra PDL eller forrige vedtak (henter forel??pig fra PDL)
    var antall = persondata.getMottakerAntallBarnIHusstand();
    var mapper = new ObjectMapper();
    return new Grunnlag(referanse, type, mapper.valueToTree(new BarnIHusstand(rolle, datoFom, null, antall)));
  }

  private String sjekkDatoRef(BeregnForskuddGrunnlagType generellInfo, LocalDate virkningsdato) {
    if (generellInfo.getDato()) {
      return "_" + virkningsdato.format(DateTimeFormatter.BASIC_ISO_DATE);
    } else {
      return "";
    }
  }

  private String lagForslagTilNyttVedtak(AktivtVedtak aktivtVedtak, BeregnetForskuddResultat beregnForskuddResponse) {
    if (beregnForskuddResponse.getBeregnetForskuddPeriodeListe().isEmpty()) {
      return ("Vedtak med vedtakId " + aktivtVedtak.getVedtakId() + " og l??pende forskudd " + aktivtVedtak.getBelop().intValueExact() + ": "
          + "Ikke mulig ?? beregne nytt forskuddsbel??p");
    }

    var nyttBelop = beregnForskuddResponse.getBeregnetForskuddPeriodeListe().get(0).getResultat().getBelop().intValueExact();
    var nyResultatkode = beregnForskuddResponse.getBeregnetForskuddPeriodeListe().get(0).getResultat().getKode();

    if (nyttBelop == 0) {
      return (standardmelding(aktivtVedtak) + "Forslag om opph??r fordi nytt beregnet bel??p er 0");
    } else if (nyttBelop > aktivtVedtak.getBelop().intValueExact()) {
      return (standardmelding(aktivtVedtak) + "Forslag om ?? sette opp l??pende forskudd til " + nyttBelop + ", med resultatkode " + nyResultatkode);
    } else if (nyttBelop < aktivtVedtak.getBelop().intValueExact()) {
      return (standardmelding(aktivtVedtak) + "Forslag om ?? sette ned l??pende forskudd til " + nyttBelop + ", med resultatkode " + nyResultatkode);
    } else {
      return (standardmelding(aktivtVedtak) + "Forslag om ?? beholde eksisterende l??pende forskudd");
    }
  }

  private static String standardmelding(AktivtVedtak aktivtVedtak) {
    return LocalTime.now().format(DateTimeFormatter.ISO_TIME) + ": Vedtak med aktivtVedtakId " + aktivtVedtak.getAktivtVedtakId()
        + " og l??pende forskudd " + aktivtVedtak.getBelop().intValueExact() + ": ";
  }
}
