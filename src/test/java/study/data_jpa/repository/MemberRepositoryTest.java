package study.data_jpa.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장 }
    }

    @Test
    public void bulkUpdate() throws Exception {

        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    // 쿼리 힌트 테스트
    @Test
    public void queryHint() throws Exception {

        //given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");

        em.flush(); // Update Query 실행X
    }

    // 락 테스트
    @Test
    public void lock() {

        //given
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        em.flush();
        em.clear();

        //when
        List<Member> result = memberRepository.findLockByUsername("member1");
    }

    // 사용자 정의 레포 테스트
    @Test
    public void callCustom() {

        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void specBasic() throws Exception {

        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

        // when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void basic() throws Exception {

        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();

        // when
        // Probe 생성
        Member member = new Member("m1");
        Team team = new Team("teamA"); // 내부조인으로 teamA 가능
        member.setTeam(team);

        // ExampleMatcher 생성, age 프로퍼티는 무시
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);

        // then
        assertThat(result.size()).isEqualTo(1);
    }
}
