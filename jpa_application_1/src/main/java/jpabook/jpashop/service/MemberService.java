package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 기본적으로 public 메서드에 적용을 해주고 메서드에 따로 설정해주면 해당 설정값으로 적용되어짐
// @AllArgsConstructor // 필드를 가지고 생성자를 만들어줌
@RequiredArgsConstructor // final을 가진 필드의 생성자를 만들어줌
public class MemberService {

    private final MemberRepository memberRepository;

    // 생성자가 1개인 경우에는 @Autowired 어노테이션을 빼도 된다.
    /*@Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }*/

    // 회원 가입
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 예제는 간단하게 중복된 회원을 검증하지만, 실무에서는 동시성 문제가 발생할 수 있기 때문에 해당 컬럼을 유니크 제약 조건을 걸어두는 것이 좋다.
     * **/
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getUsername());
        if (!findMembers.isEmpty()) throw new IllegalStateException("이미 존재하는 회원입니다.");
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
