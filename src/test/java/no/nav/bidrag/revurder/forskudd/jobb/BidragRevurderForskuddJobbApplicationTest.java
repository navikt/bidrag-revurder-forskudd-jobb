package no.nav.bidrag.revurder.forskudd.jobb;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BidragRevurderForskuddJobbLocal.class, webEnvironment = RANDOM_PORT)
class BidragRevurderForskuddJobbApplicationTest {

  @Test
  void contextLoads() {
  }
}
