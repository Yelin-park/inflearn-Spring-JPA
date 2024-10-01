package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /** MemberForm의 name은 필수로 지정하여 @Valid를 사용하면 Validation 할 수 있음
     * 서버사이드를 통해서 바인딩 되어서 데이터를 받게 되는데 BindingResult를 사용하여 Error가 있으면
     * 회원가입 폼을 보여주면서 @NotEmpty(message = "회원 이름은 필수 입니다.") 에러를 가져가서 보여줌
     * 해당 에러를 보여줄 수 있는 것은 createMemberForm에 th:errors="*{name} 값 등 설정을 하고 있어서 가능함
     * */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) return "members/createMemberForm";

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setUsername(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";
    }

    /** Entity는 순수하게 유지를 해줘야한다. 변경이 필요한 경우에는 DTO에 담아서 넘겨야한다.
     * API를 만들 때는 웹으로 절대 Entity를 반환하면 안된다. */
    @GetMapping("/members")
    public String list(Model model) {
        model.addAttribute("members", memberService.findMembers());
        return "members/memberList";
    }
}
