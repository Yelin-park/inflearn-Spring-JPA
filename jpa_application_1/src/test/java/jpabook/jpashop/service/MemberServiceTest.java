package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    //@Rollback(false) // 롤백 안하고 커밋을 함
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("park");

        //when
        Long savedId = memberService.join(member);

        //then
        em.flush(); // 롤백을 하더라도 쿼리문을 확인할 수 있음
        Assert.assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class) // expected를 넣어주면 아래 try-catch 문을 안써도 된다.
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setUsername("park");

        Member member2 = new Member();
        member2.setUsername("park");

        //when
        memberService.join(member1);
        memberService.join(member2); // 예외 발생
        /*try {
            memberService.join(member2);
        } catch (IllegalStateException e) {
            return;
        }*/

        //then
        fail("예외가 발생해야 한다.");
    }

}