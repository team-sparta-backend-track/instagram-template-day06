package com.example.instagramclone.service;

import com.example.instagramclone.domain.member.dto.request.LoginRequest;
import com.example.instagramclone.domain.member.dto.request.SignUpRequest;
import com.example.instagramclone.domain.member.dto.response.SessionUser;
import com.example.instagramclone.domain.member.entity.Member;
import com.example.instagramclone.exception.CommonErrorCode;
import com.example.instagramclone.exception.ErrorCode;
import com.example.instagramclone.exception.MemberErrorCode;
import com.example.instagramclone.exception.MemberException;
import com.example.instagramclone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        String emailOrPhone = signUpRequest.emailOrPhone();
        String email = null;
        String phone = null;

        // 이메일/전화번호 구분 및 중복체크
        if (emailOrPhone.contains("@")) {
            email = emailOrPhone;
            if (memberRepository.existsByEmail(email)) {
                throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
            }
        } else {
            phone = emailOrPhone;
            if (memberRepository.existsByPhone(phone)) {
                throw new MemberException(MemberErrorCode.DUPLICATE_PHONE);
            }
        }

        // 사용자 이름 중복체크
        if (memberRepository.existsByUsername(signUpRequest.username())) {
            throw new MemberException(MemberErrorCode.DUPLICATE_USERNAME);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequest.password());

        // Entity 변환
        Member member = Member.builder()
                .username(signUpRequest.username())
                .password(encodedPassword)
                .email(email)
                .phone(phone)
                .name(signUpRequest.name())
                .build();

        // 저장
        memberRepository.save(member);
    }



    public boolean checkDuplicate(String type, String value) {
        return switch (type) {
            case "username" -> !memberRepository.existsByUsername(value);
            case "email" -> !memberRepository.existsByEmail(value);
            case "phone" -> !memberRepository.existsByPhone(value);
            default -> throw new MemberException(CommonErrorCode.INVALID_INPUT_VALUE);
        };
    }

    public SessionUser login(LoginRequest request) {
        String identifier = request.username(); // 클라이언트가 보낸 입력값 (아이디 or 이메일 or 폰)
        Member member;

        // 1. 입력값의 형태를 보고 어떤 컬럼에서 찾을지 판단합니다.
        if (identifier.contains("@")) {
            member = memberRepository.findByEmail(identifier)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_CREDENTIALS));
        } else if (identifier.matches("^[0-9]+$")) { // 숫자만 있다면 전화번호!
            member = memberRepository.findByPhone(identifier)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_CREDENTIALS));
        } else {
            member = memberRepository.findByUsername(identifier)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_CREDENTIALS));
        }

        // 2. 비밀번호가 맞는지 확인 (암호화된 비번끼리 비교해야 하니 encoder 필수!)
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.INVALID_CREDENTIALS);
        }

        return SessionUser.from(member);
    }



    // 타 도메인(Service)에서 내부적인 비즈니스 로직을 위해 Member 엔티티가 필요할 때 호출
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

}
