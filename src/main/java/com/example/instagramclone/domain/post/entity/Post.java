package com.example.instagramclone.domain.post.entity;

import com.example.instagramclone.domain.common.BaseEntity;
import com.example.instagramclone.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 누가 썼는가? (회원 1 : 게시물 N)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member writer;

    // 어떤 사진들이 있는가? (게시물 1 : 사진 N)
    @OneToMany(mappedBy = "post") // "나는 관계의 주인이 아니야!" 선언
    private List<PostImage> images = new ArrayList<>();

    @Builder
    public Post(String content, Member writer) {
        this.content = content;
        this.writer = writer;
    }
}