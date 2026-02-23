package com.example.instagramclone.service;

import com.example.instagramclone.domain.member.entity.Member;
import com.example.instagramclone.domain.post.dto.request.PostCreateRequest;
import com.example.instagramclone.domain.post.entity.Post;
import com.example.instagramclone.domain.post.entity.PostImage;
import com.example.instagramclone.exception.MemberErrorCode;
import com.example.instagramclone.exception.MemberException;
import com.example.instagramclone.repository.PostImageRepository;
import com.example.instagramclone.repository.PostRepository;
import com.example.instagramclone.util.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final MemberService memberService; // MemberRepository가 아닙니다!
    private final FileStore fileStore;


    @Transactional
    public void create(PostCreateRequest request, List<MultipartFile> images, Long loginMemberId) throws IOException {
        // 1. 요청 인가(Authorization): 한 번 더 확실하게 검증합니다.
        if (loginMemberId == null) {
            throw new MemberException(MemberErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 2. 세션의 회원 ID로 Member 엔티티 획득.
        Member writer = memberService.getMemberById(loginMemberId);

        // 3. Post(부모) 엔터티 생성
        Post post = Post.builder()
                .content(request.content())
                .writer(writer)
                .build();

        // 4. Post 1차 저장: 이 순간 DB에 INSERT 되고, post에 id(PK)가 부여됩니다!
        Post savedPost = postRepository.save(post);

        // 5. 업로드된 이미지 파일 저장 및 PostImage 생성 (Stream & Lambda 적용)
        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = IntStream.range(0, images.size())
                    .mapToObj(i -> {
                        try {
                            // 6. FileStore를 이용해 실제 디스크에 저장 후 URL 반환
                            String imageUrl = fileStore.storeFile(images.get(i));

                            // 7. PostImage 엔티티 생성 및 Post와 연관관계 설정
                            return PostImage.builder()
                                    .post(savedPost)
                                    .imageUrl(imageUrl)
                                    .imgOrder(i + 1) // 1부터 시작하는 순서
                                    .build();
                        } catch (IOException e) {
                            // 💡 람다 내부에서는 Checked Exception을 바로 던질 수 없으므로 RuntimeException으로 감싸서 던짐!
                            throw new RuntimeException("피드 이미지 저장 중 오류가 발생했습니다.", e);
                        }
                    })
                    .toList(); // Java 16+ 불변 리스트 반환

            // 8. 명시적으로 PostImage 엔티티들을 몽땅 저장 (수동 저장의 극치!)
            postImageRepository.saveAll(postImages);

        }
    }
}