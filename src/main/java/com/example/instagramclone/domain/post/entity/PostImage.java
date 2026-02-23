package com.example.instagramclone.domain.post.entity;

import com.example.instagramclone.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // 사진의 접속 경로
    private Integer imgOrder; // 사진 순서

    // 어느 게시물에 속해있는가? (사진 N : 게시물 1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false) // 실제 DB의 외래 키(FK)
    private Post post;

    @Builder
    public PostImage(String imageUrl, Integer imgOrder, Post post) {
        this.imageUrl = imageUrl;
        this.imgOrder = imgOrder;
        this.post = post;
    }
}