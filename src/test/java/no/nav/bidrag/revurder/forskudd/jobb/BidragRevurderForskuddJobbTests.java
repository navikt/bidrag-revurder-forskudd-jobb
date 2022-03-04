package no.nav.bidrag.revurder.forskudd.jobb;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BidragRevurderForskuddJobbLocal.class, webEnvironment = RANDOM_PORT)
class BidragRevurderForskuddJobbTests {

  @Test
  void contextLoads() {
  }
}
